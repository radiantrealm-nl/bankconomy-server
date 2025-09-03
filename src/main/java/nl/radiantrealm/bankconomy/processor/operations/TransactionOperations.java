package nl.radiantrealm.bankconomy.processor.operations;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsAccountCache;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.bankconomy.record.Transaction;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.DataObject;

import java.sql.Connection;
import java.sql.SQLException;

public record TransactionOperations(Transaction transaction) implements ProcessHandler {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final SavingsAccountCache savingsAccountCache = Main.savingsAccountCache;

    public TransactionOperations(JsonObject object) throws Exception {
        this(
                DataObject.fromJson(Transaction.class, object)
        );
    }

    @Override
    public ProcessResult handle(Process process) throws Exception {
        if (!transaction.isValidAmount(2, true)) {
            return ProcessResult.error(400, "Invalid transaction amount.");
        }

        return switch (transaction.transactionType()) {
            case SAVINGS_DEPOSIT -> handleSavingsDeposit(transaction);
            case SAVINGS_WITHDRAW -> handleSavingsWithdraw(transaction);
            case PAY_PLAYER -> handlePayPlayer(transaction);

            default -> ProcessResult.error(400, "Invalid process type.");
        };
    }

    private ProcessResult handleSavingsDeposit(Transaction transaction) throws Exception {
        PlayerAccount playerAccount = playerAccountCache.get(transaction.sourceUUID());

        if (playerAccount == null) {
            return ProcessResult.error(404, "Could not find player account.");
        }

        SavingsAccount savingsAccount = savingsAccountCache.get(transaction.offsetUUID());

        if (savingsAccount == null) {
            return ProcessResult.error(404, "Could not find savings account.");
        }

        if (!playerAccount.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error(422, "Insufficient balance.");
        }

        playerAccount = playerAccount.subtractBalance(transaction.transactionAmount());
        savingsAccount = savingsAccount.addBalance(transaction.transactionAmount());

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updatePlayerBalance(connection, playerAccount);
            Database.updateSavingsBalance(connection, savingsAccount);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e);
        }

        playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
        savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);
        return ProcessResult.ok();
    }

    private ProcessResult handleSavingsWithdraw(Transaction transaction) throws Exception {
        SavingsAccount savingsAccount = savingsAccountCache.get(transaction.sourceUUID());

        if (savingsAccount == null) {
            return ProcessResult.error(404, "Could not find savings account.");
        }

        PlayerAccount playerAccount = playerAccountCache.get(transaction.offsetUUID());

        if (playerAccount == null) {
            return ProcessResult.error(404, "Could not find player account.");
        }

        if (!savingsAccount.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error(422, "Insufficient balance.");
        }

        savingsAccount = savingsAccount.subtractBalance(transaction.transactionAmount());
        savingsAccount = savingsAccount.subtractBalance(transaction.transactionAmount());

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updatePlayerBalance(connection, playerAccount);
            Database.updateSavingsBalance(connection, savingsAccount);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e);
        }

        playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
        savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);
        return ProcessResult.ok();
    }

    private ProcessResult handlePayPlayer(Transaction transaction) throws Exception {
        PlayerAccount sourcePlayerAccount = playerAccountCache.get(transaction.sourceUUID());
        PlayerAccount offsetPlayerAccount = playerAccountCache.get(transaction.offsetUUID());

        if (sourcePlayerAccount == null || offsetPlayerAccount == null) {
            return ProcessResult.error(404, "Could not find player account.");
        }

        if (!sourcePlayerAccount.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error(422, "Insufficient balance.");
        }

        sourcePlayerAccount = sourcePlayerAccount.subtractBalance(transaction.transactionAmount());
        offsetPlayerAccount = offsetPlayerAccount.addBalance(transaction.transactionAmount());

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updatePlayerBalance(connection, sourcePlayerAccount);
            Database.updatePlayerBalance(connection, offsetPlayerAccount);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException(e);
        }

        playerAccountCache.put(sourcePlayerAccount.playerUUID(), sourcePlayerAccount);
        playerAccountCache.put(offsetPlayerAccount.playerUUID(), offsetPlayerAccount);
        return ProcessResult.ok();
    }
}
