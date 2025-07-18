package nl.radiantrealm.bankconomy.processor;

import nl.radiantrealm.bankconomy.controller.Database;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessRequest;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.sql.TransactionBuilder;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;

public class CreatePlayerAccount extends ProcessHandler<CreatePlayerAccount> {
    private final UUID playerUUID;
    private final String playerName;

    public CreatePlayerAccount(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    @Override
    public ProcessResult handle(ProcessRequest<CreatePlayerAccount> processRequest) {
        PlayerAccount playerAccount = new PlayerAccount(playerUUID, BigDecimal.ZERO, playerName);

        try (Connection connection = Database.getConnection(false)) {
            TransactionBuilder.prepare(connection, "INSERT INTO bankconomy_players VALUES (?, ?, ?)")
                    .setUUID(1, playerAccount.playerUUID())
                    .setBigDecimal(2, playerAccount.playerBalance())
                    .setString(3, playerAccount.playerName())
                    .execute();
        } catch (Exception e) {
            return ProcessResult.failure(e);
        }

        return ProcessResult.ok();
    }
}
