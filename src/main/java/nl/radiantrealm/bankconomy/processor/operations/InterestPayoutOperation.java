package nl.radiantrealm.bankconomy.processor.operations;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
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

public class InterestPayoutOperation implements ProcessHandler {

    @Override
    public ProcessResult handle(Process process) throws Exception {
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

        Map<UUID, Transaction> transactionMap = new HashMap<>(savingsAccountList.size());

        for (SavingsAccount savingsAccount : savingsAccountList) {
            BigDecimal payout = savingsAccount.accumulatedInterest().setScale(2, RoundingMode.HALF_UP);
            savingsAccount = savingsAccount.payoutInterest(payout);

            transactionMap.put(savingsAccount.savingsUUID(), new Transaction(
                    TransactionType.INTEREST_PAYOUT,
                    payout,
                    savingsAccount.savingsUUID(),
                    savingsAccount.savingsUUID(),
                    ""
            ));
        }

        Connection connection = Database.getConnection();

        try (connection) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE bankconomy_savings SET savings_balance = ?, accumulated_interest = ? WHERE savings_uuid = ?"
            );

            for (SavingsAccount savingsAccount : savingsAccountList) {
                Database.insertTransactionLog(connection, transactionMap.get(savingsAccount.savingsUUID()).createTransactionLog());

                statement.setBigDecimal(1, savingsAccount.savingsBalance());
                statement.setBigDecimal(2, savingsAccount.accumulatedInterest());
                statement.setString(3, savingsAccount.savingsUUID().toString());
                statement.executeUpdate();
                statement.clearParameters();
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            return ProcessResult.error("Database error.", e);
        }

        savingsAccountList.forEach(savingsAccount -> Main.savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount));

        return ProcessResult.ok();
    }
}
