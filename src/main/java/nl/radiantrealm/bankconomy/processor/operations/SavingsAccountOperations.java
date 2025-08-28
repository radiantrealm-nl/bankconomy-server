package nl.radiantrealm.bankconomy.processor.operations;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.enumerator.AuditType;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.bankconomy.record.AuditLog;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.bankconomy.record.Transaction;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.UUID;

public class SavingsAccountOperations {

    public static class CreateAccount implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            JsonObject object = process.object();

            UUID savingsUUID = UUID.randomUUID();
            UUID ownerUUID = JsonUtils.getJsonUUID(object, "owner_uuid");
            String savingsName = JsonUtils.getJsonString(object, "savings_name");

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bankconomy_savings VALUES (?, ?, ?, ?, ?, ?)"
                );

                statement.setString(1, savingsUUID.toString());
                statement.setString(2, ownerUUID.toString());
                statement.setString(3, savingsName);
                statement.setBigDecimal(4, BigDecimal.ZERO);
                statement.setBigDecimal(5, BigDecimal.ZERO);
                statement.setString(6, savingsName);
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.CREATE_SAVINGS_ACCOUNT,
                        savingsUUID,
                        savingsName
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return ProcessResult.error("Database error.", e);
            }

            JsonObject response = new JsonObject();
            response.addProperty("savings_uuid", savingsUUID.toString());
            return new ProcessResult(true, Optional.of(response), Optional.empty());
        }
    }

    public static class UpdateName implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            JsonObject object = process.object();

            UUID savingsUUID = JsonUtils.getJsonUUID(object, "savings_uuid");
            String savingsName = JsonUtils.getJsonString(object, "savings_name");

            SavingsAccount savingsAccount = Main.savingsAccountCache.get(savingsUUID);

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bankconomy_savings SET savings_name = ? WHERE savings_uuid = ?"
                );

                statement.setString(1, savingsName);
                statement.setString(2, savingsUUID.toString());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.UPDATE_SAVINGS_NAME,
                        savingsUUID,
                        savingsName
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return ProcessResult.error("Database error.", e);
            }

            Main.savingsAccountCache.put(savingsUUID, savingsAccount.updateName(savingsName));

            return ProcessResult.ok();
        }
    }

    public static class DeleteAccount implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            JsonObject object = process.object();

            UUID savingsUUID = JsonUtils.getJsonUUID(object, "savings_uuid");

            SavingsAccount savingsAccount = Main.savingsAccountCache.get(savingsUUID);
            PlayerAccount playerAccount = Main.playerAccountCache.get(savingsAccount.ownerUUID());

            BigDecimal payout = BigDecimal.ZERO
                    .add(savingsAccount.savingsBalance())
                    .add(savingsAccount.accumulatedInterest());

            Transaction transaction = new Transaction(
                    TransactionType.SAVINGS_DELETE,
                    payout,
                    savingsAccount.savingsUUID(),
                    playerAccount.playerUUID(),
                    ""
            );

            playerAccount = playerAccount.addBalance(payout);

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bankconomy_savings WHERE savings_uuid = ?"
                );

                statement.setString(1, savingsUUID.toString());
                statement.executeUpdate();

                Database.updatePlayerBalance(connection, playerAccount);
                Database.insertTransactionLog(connection, transaction.createTransactionLog());
                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.DELETE_SAVINGS_ACCOUNT,
                        savingsUUID,
                        savingsAccount.savingsName()
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return ProcessResult.error("Database error.", e);
            }

            Main.savingsAccountCache.remove(savingsUUID);
            Main.savingsOwnerCache.remove(savingsAccount.ownerUUID());

            return ProcessResult.ok();
        }
    }
}
