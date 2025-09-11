package nl.radiantrealm.bankconomy.processor.operations;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.enumerator.AuditType;
import nl.radiantrealm.bankconomy.record.AuditLog;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerAccountOperations {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;

    public record CreateAccount(UUID playerUUID, String playerName) implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            PlayerAccount playerAccount = new PlayerAccount(
                    playerUUID,
                    BigDecimal.ZERO,
                    playerName
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
                throw new SQLException(e);
            }

            playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
            return ProcessResult.ok();
        }
    }

    public record UpdateName(UUID playerUUID, String playerName) implements ProcessHandler {

        @Override
        public ProcessResult handle(Process process) throws Exception {
            PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

            if (playerAccount == null) {
                return ProcessResult.error(404, "Could not find player account.");
            }

            playerAccount = playerAccount.updateName(playerName);

            Connection connection = Database.getConnection(false);

            try (connection) {
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE bankconomy_players SET player_name = ? WHERE player_uuid = ?"
                );

                statement.setString(1, playerAccount.playerName());
                statement.setString(2, playerAccount.playerUUID().toString());
                statement.executeUpdate();

                Database.insertAuditLog(connection, AuditLog.createAuditLog(
                        AuditType.UPDATE_PLAYER_NAME,
                        playerUUID,
                        playerName
                ));

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new SQLException(e);
            }

            playerAccountCache.put(playerAccount.playerUUID(), playerAccount);
            return ProcessResult.ok();
        }
    }
}
