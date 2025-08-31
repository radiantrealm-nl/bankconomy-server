package nl.radiantrealm.bankconomy.record;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record PlayerAccount(UUID playerUUID, BigDecimal playerBalance, String playerName) implements DataObject {

    public PlayerAccount(JsonObject object) throws IllegalArgumentException {
        this(
                JsonUtils.getJsonUUID(object, "player_uuid"),
                JsonUtils.getJsonBigDecimal(object, "player_balance"),
                JsonUtils.getJsonString(object, "player_name")
        );
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("player_uuid", playerUUID.toString());
        object.addProperty("player_balance", playerBalance);
        object.addProperty("player_name", playerName);
        return object;
    }

    public PlayerAccount addBalance(BigDecimal amount) {
        return new PlayerAccount(playerUUID, playerBalance.add(amount), playerName);
    }

    public PlayerAccount subtractBalance(BigDecimal amount) {
        return new PlayerAccount(playerUUID, playerBalance.subtract(amount), playerName);
    }

    public PlayerAccount updateName(String playerName) {
        return new PlayerAccount(playerUUID, playerBalance, playerName);
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return playerBalance.compareTo(amount) >= 0;
    }
}
