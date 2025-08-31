package nl.radiantrealm.bankconomy.processor.operations;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsOwnerCache;
import nl.radiantrealm.bankconomy.enumerator.AuditType;
import nl.radiantrealm.bankconomy.record.AuditLog;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

public class PlayerAccountOperations {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final SavingsOwnerCache savingsOwnerCache = Main.savingsOwnerCache;

    public static class CreateAccount implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            JsonObject object = process.object();

            PlayerAccount playerAccount = new PlayerAccount(
                    JsonUtils.getJsonUUID(object, "player_uuid"),
                    BigDecimal.ZERO,
                    JsonUtils.getJsonString(object, "player_name")
            );

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bankconomy_players VALUES (?, ?, ?)"
                );

                statement.setString(1, playerAccount.playerUUID().toString());
                statement.setBigDecimal(2, playerAccount.playerBalance());
                statement.setString(3, playerAccount.playerName());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.CREATE_PLAYER_ACCOUNT,
                        playerAccount.playerUUID(),
                        playerAccount.playerName()
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return ProcessResult.error("Database error.", e);
            }

            playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
            savingsOwnerCache.put(playerAccount.playerUUID(), List.of());

            return ProcessResult.ok();
        }
    }

    public static class UpdateName implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            JsonObject object = process.object();

            UUID playerUUID = JsonUtils.getJsonUUID(object, "player_uuid");
            String playerName = JsonUtils.getJsonString(object, "player_name");

            PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bankconomy_players SET player_name = ? WHERE player_uuid = ?"
                );

                statement.setString(1, playerName);
                statement.setString(2, playerUUID.toString());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.UPDATE_PLAYER_NAME,
                        playerUUID,
                        playerName
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                return ProcessResult.error("Database error.", e);
            }

            playerAccountCache.put(playerUUID, playerAccount.updateName(playerName));

            return ProcessResult.ok();
        }
    }
}
