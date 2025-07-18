package nl.radiantrealm.bankconomy.cache;

import nl.radiantrealm.bankconomy.controller.Database;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.cache.CacheRegistry;
import nl.radiantrealm.library.sql.TransactionBuilder;
import nl.radiantrealm.library.sql.TransactionResult;
import nl.radiantrealm.library.utils.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class PlayerAccountCache extends CacheRegistry<UUID, PlayerAccount> {

    public PlayerAccountCache() {
        super(15);
    }

    @Override
    protected Result<PlayerAccount> load(UUID uuid) {
        try (Connection connection = Database.getConnection(true)) {
            TransactionResult result = TransactionBuilder.prepare(connection, "SELECT * FROM bankconomy_players WHERE player_uuid = ?")
                    .setUUID(1, uuid)
                    .executeQuery();

            if (result.rs().next()) { //Maybe add .next method to result?
                return Result.ok(new PlayerAccount(
                        uuid,
                        result.getBigDecimal("player_balance"),
                        result.getString("player_name")
                ));
            }

            return Result.ok(null); //Add empty method devs pleaseee
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    @Override
    protected Map<UUID, Optional<PlayerAccount>> load(List<UUID> list) throws Exception {
        String placeholders = String.join(", ", list.stream().map(uuid -> "?").toArray(String[]::new));

        try (Connection connection = Database.getConnection(true)) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM bankconomy_players WHERE player_uuid IN (" + placeholders + ")"
            );

            for (int i = 0; i < list.size(); i++) {
                statement.setString(i + 1, list.get(i).toString());
            }

            ResultSet rs = statement.executeQuery();
            Map<UUID, Optional<PlayerAccount>> result = new HashMap<>();

            while (rs.next()) {
                UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));

                try {
                    result.put(playerUUID, Optional.of(new PlayerAccount(
                            playerUUID,
                            rs.getBigDecimal("player_balance"),
                            rs.getString("player_name")
                    )));
                } catch (Exception e) {
                    result.put(playerUUID, Optional.empty());
                }
            }

            return result;
        }
    }
}
