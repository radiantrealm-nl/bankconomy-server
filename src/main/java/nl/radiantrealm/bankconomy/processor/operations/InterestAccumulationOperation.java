package nl.radiantrealm.bankconomy.processor.operations;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.enumerator.GovernmentUUID;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.bankconomy.record.GovernmentFunds;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.bankconomy.record.Transaction;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.FormatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class InterestAccumulationOperation implements ProcessHandler {
    private static final BigDecimal sectorRate1 = new BigDecimal("0.0150");
    private static final BigDecimal sectorRate2 = new BigDecimal("0.0125");
    private static final BigDecimal sectorRate3 = new BigDecimal("0.0100");

    private static final BigDecimal sectorBound1 = new BigDecimal(10000);
    private static final BigDecimal sectorBound2 = new BigDecimal(25000);
    private static final BigDecimal sectorBound3 = new BigDecimal(100000);

    @Override
    public ProcessResult handle(Process process) throws Exception {
        GovernmentFunds governmentFunds = Main.governmentFundsCache.get(GovernmentUUID.GOVERNMENT_FUND);
        List<SavingsAccount> savingsAccountList = new ArrayList<>();

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM bankconomy_savings"
            );

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                savingsAccountList.add(new SavingsAccount(
                        FormatUtils.formatUUID(rs.getString("savings_uuid")),
                        FormatUtils.formatUUID(rs.getString("owner_uuid")),
                        rs.getBigDecimal("savings_balance"),
                        rs.getBigDecimal("accumulated_interest"),
                        rs.getString("savings_name")
                ));
            }
        }

        Map<UUID, BigDecimal> playerSavingsNetWorth = new HashMap<>();

        for (SavingsAccount savingsAccount : savingsAccountList) {
            playerSavingsNetWorth.merge(
                    savingsAccount.ownerUUID(),
                    savingsAccount.savingsBalance(),
                    BigDecimal::add
            );
        }

        Map<UUID, BigDecimal> blendedInterestRate = new HashMap<>();

        for (Map.Entry<UUID, BigDecimal> entry : playerSavingsNetWorth.entrySet()) {
            BigDecimal remaining = entry.getValue();
            BigDecimal accumulation = BigDecimal.ZERO;

            BigDecimal acc1 = remaining.min(sectorBound1).multiply(sectorRate1);
            remaining = remaining.subtract(acc1);
            accumulation = accumulation.add(acc1);

            BigDecimal acc2 = remaining.min(sectorBound2).multiply(sectorRate2);
            remaining = remaining.subtract(acc2);
            accumulation = accumulation.add(acc2);

            BigDecimal acc3 = remaining.min(sectorBound3).multiply(sectorRate3);
            accumulation = accumulation.add(acc3);

            blendedInterestRate.put(entry.getKey(), accumulation.divide(entry.getValue(), 5, RoundingMode.HALF_UP));
        }

        Map<UUID, Transaction> transactionMap = new HashMap<>();

        for (SavingsAccount savingsAccount : savingsAccountList) {
            BigDecimal interestRate = blendedInterestRate.get(savingsAccount.ownerUUID());
            BigDecimal interestAccumulation = savingsAccount.savingsBalance().multiply(interestRate)
                    .setScale(5, RoundingMode.HALF_UP);

            transactionMap.put(savingsAccount.savingsUUID(), new Transaction(
                    TransactionType.INTEREST_ACCUMULATION,
                    interestAccumulation,
                    GovernmentUUID.GOVERNMENT_FUND.uuid,
                    savingsAccount.savingsUUID(),
                    ""
            ));

            savingsAccountList.add(savingsAccount.accumulateInterest(interestAccumulation));
            governmentFunds = governmentFunds.subtractBalance(interestAccumulation);
        }

        Connection connection = Database.getConnection(false);

        try (connection) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE bankconomy_savings SET accumulated_interest = ? WHERE savings_uuid = ?"
            );

            for (SavingsAccount savingsAccount : savingsAccountList) {
                Database.insertTransactionLog(connection, transactionMap.get(savingsAccount.savingsUUID()).createTransactionLog());

                statement.setBigDecimal(1, savingsAccount.savingsBalance());
                statement.setString(2, savingsAccount.savingsUUID().toString());
                statement.executeUpdate();
                statement.clearParameters();
            }

            Database.updateGovernmentFunds(connection, governmentFunds);

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            return ProcessResult.error("Database error.", e);
        }

        Main.governmentFundsCache.put(governmentFunds.governmentUUID(), governmentFunds);
        savingsAccountList.forEach(savingsAccount -> Main.savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount));

        return ProcessResult.ok();
    }
}
