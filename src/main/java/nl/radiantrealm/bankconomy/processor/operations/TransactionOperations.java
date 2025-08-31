package nl.radiantrealm.bankconomy.processor.operations;

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

public class TransactionOperations implements ProcessHandler {
    private final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private final SavingsAccountCache savingsAccountCache = Main.savingsAccountCache;

    @Override
    public ProcessResult handle(Process process) throws Exception {
        Transaction transaction = DataObject.fromJson(Transaction.class, process.object());

        return switch (transaction.transactionType()) {
            case SAVINGS_DEPOSIT -> handleSavingsDeposit(transaction);
            case SAVINGS_WITHDRAW -> handleSavingsWithdraw(transaction);
            case PAY_PLAYER -> handlePayPlayer(transaction);
            default -> ProcessResult.error("Invalid transaction type.");
        };
    }

    private ProcessResult handleSavingsDeposit(Transaction transaction) throws Exception {
        PlayerAccount playerAccount = playerAccountCache.get(transaction.sourceUUID());
        SavingsAccount savingsAccount = savingsAccountCache.get(transaction.offsetUUID());

        if (!playerAccount.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error("Insufficient balance.");
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
            return ProcessResult.error("Database error.", e);
        }

        playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
        savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);

        return ProcessResult.ok();
    }

    private ProcessResult handleSavingsWithdraw(Transaction transaction) throws Exception {
        SavingsAccount savingsAccount = savingsAccountCache.get(transaction.sourceUUID());
        PlayerAccount playerAccount = playerAccountCache.get(transaction.offsetUUID());

        if (!savingsAccount.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error("Insufficient balance.");
        }

        savingsAccount = savingsAccount.subtractBalance(transaction.transactionAmount());
        playerAccount = playerAccount.addBalance(transaction.transactionAmount());

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updateSavingsBalance(connection, savingsAccount);
            Database.updatePlayerBalance(connection, playerAccount);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            return ProcessResult.error("Database error.", e);
        }

        savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);
        playerAccountCache.put(playerAccount.playerUUID(), playerAccount);

        return ProcessResult.ok();
    }

    private ProcessResult handlePayPlayer(Transaction transaction) throws Exception {
        PlayerAccount playerAccountSource = playerAccountCache.get(transaction.sourceUUID());
        PlayerAccount playerAccountOffset = playerAccountCache.get(transaction.offsetUUID());

        if (!playerAccountSource.hasSufficientBalance(transaction.transactionAmount())) {
            return ProcessResult.error("Insufficient balance.");
        }

        playerAccountSource = playerAccountSource.subtractBalance(transaction.transactionAmount());
        playerAccountOffset = playerAccountOffset.addBalance(transaction.transactionAmount());

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updatePlayerBalance(connection, playerAccountSource);
            Database.updatePlayerBalance(connection, playerAccountOffset);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            return ProcessResult.error("Database error.", e);
        }

        playerAccountCache.put(playerAccountSource.playerUUID(), playerAccountSource);
        playerAccountCache.put(playerAccountOffset.playerUUID(), playerAccountOffset);

        return ProcessResult.ok();
    }
}
