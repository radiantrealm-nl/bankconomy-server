package nl.radiantrealm.bankconomy.processor.operations;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsAccountCache;
import nl.radiantrealm.bankconomy.enumerator.AuditType;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.bankconomy.record.*;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SavingsAccountOperations {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final SavingsAccountCache savingsAccountCache = Main.savingsAccountCache;

    public record CreateAccount(UUID ownerUUID, String savingsName) implements ProcessHandler {

        public CreateAccount(JsonObject object) {
            this(
                    JsonUtils.getJsonUUID(object, "owner_uuid"),
                    JsonUtils.getJsonString(object, "savings_name")
            );
        }

        @Override
        public ProcessResult handle(Process process) throws Exception {
            SavingsAccount savingsAccount = new SavingsAccount(
                    UUID.randomUUID(),
                    ownerUUID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    savingsName
            );

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bankconomy_savings VALUES (?, ?, ?, ?, ?)"
                );

                statement.setString(1, savingsAccount.savingsUUID().toString());
                statement.setString(2, savingsAccount.ownerUUID().toString());
                statement.setBigDecimal(3, savingsAccount.savingsBalance());
                statement.setBigDecimal(4, savingsAccount.accumulatedInterest());
                statement.setString(5, savingsAccount.savingsName());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.UPDATE_SAVINGS_NAME,
                        savingsAccount.savingsUUID(),
                        savingsAccount.savingsName()
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException(e);
            }

            savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);
            return ProcessResult.ok();
        }
    }

    public record UpdateName(UUID savingsUUID, String savingsName) implements ProcessHandler {

        public UpdateName(JsonObject object) {
            this(
                    JsonUtils.getJsonUUID(object, "savings_uuid"),
                    JsonUtils.getJsonString(object, "savings_name")
            );
        }

        @Override
        public ProcessResult handle(Process process) throws Exception {
            SavingsAccount savingsAccount = savingsAccountCache.get(savingsUUID);

            if (savingsAccount == null) {
                return ProcessResult.error(404, "Could not find savings account.");
            }

            savingsAccount = savingsAccount.updateName(savingsName);

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bankconomy_savings SET savings_name = ? WHERE savings_uuid = ?"
                );

                statement.setString(1, savingsAccount.savingsName());
                statement.setString(2, savingsAccount.savingsUUID().toString());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.UPDATE_SAVINGS_NAME,
                        savingsAccount.savingsUUID(),
                        savingsAccount.savingsName()
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException(e);
            }

            savingsAccountCache.put(savingsAccount.savingsUUID(), savingsAccount);
            return ProcessResult.ok();
        }
    }

    public record DeleteAccount(UUID savingsUUID) implements ProcessHandler {

        public DeleteAccount(JsonObject object) {
            this(
                    JsonUtils.getJsonUUID(object, "savings_uuid")
            );
        }

        @Override
        public ProcessResult handle(Process process) throws Exception {
            SavingsAccount savingsAccount = savingsAccountCache.get(savingsUUID);

            if (savingsAccount == null) {
                return ProcessResult.error(404, "Could not find savings account.");
            }

            PlayerAccount playerAccount = playerAccountCache.get(savingsAccount.ownerUUID());

            if (playerAccount == null) {
                return ProcessResult.error(404, "Could not find player account.");
            }

            BigDecimal remainingBalance = savingsAccount.savingsBalance()
                    .add(savingsAccount.accumulatedInterest())
                    .setScale(2, RoundingMode.HALF_UP);

            Transaction transaction = new Transaction(
                    TransactionType.SAVINGS_DELETE,
                    remainingBalance,
                    savingsAccount.savingsUUID(),
                    savingsAccount.ownerUUID(),
                    ""
            );

            playerAccount = playerAccount.addBalance(transaction.transactionAmount());

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bankconomy_savings WHERE savings_uuid = ?"
                );

                statement.setString(1, savingsAccount.savingsUUID().toString());
                statement.executeUpdate();

                Database.updatePlayerBalance(connection, playerAccount);
                Database.insertTransactionLog(connection, transaction.createTransactionLog());
                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.DELETE_SAVINGS_ACCOUNT,
                        savingsAccount.savingsUUID(),
                        savingsAccount.savingsName()
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException(e);
            }

            savingsAccountCache.remove(savingsAccount.savingsUUID());
            return ProcessResult.ok();
        }
    }
}
