package nl.radiantrealm.bankconomy.processor;

import nl.radiantrealm.bankconomy.controller.Database;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessRequest;
import nl.radiantrealm.library.processor.ProcessResult;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public record CreatePlayerAccount(UUID playerUUID, String playerName) implements ProcessHandler<CreatePlayerAccount, Void> {

    @Override
    public ProcessResult<Void> handle(ProcessRequest<CreatePlayerAccount> processRequest) throws Exception {
        PlayerAccount playerAccount = new PlayerAccount(playerUUID, BigDecimal.ZERO, playerName);

        try (Connection connection = Database.getConnection(false)) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO bankconomy_players VALUES (?, ?, ?)"
            );

            statement.setString(1, playerAccount.playerUUID().toString());
            statement.setBigDecimal(2, playerAccount.playerBalance());
            statement.setString(3, playerAccount.playerName());
            statement.execute();
        } catch (Exception e) {
            return ProcessResult.failure(e);
        }

        return ProcessResult.success(Void); //FIX THIS BRUH
    }
}
