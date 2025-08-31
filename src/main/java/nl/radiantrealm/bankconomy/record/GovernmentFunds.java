package nl.radiantrealm.bankconomy.record;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.enumerator.GovernmentUUID;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record GovernmentFunds(UUID governmentUUID, BigDecimal governmentBalance) implements DataObject {

    public GovernmentFunds(GovernmentUUID governmentUUID, BigDecimal governmentBalance) {
        this(governmentUUID.uuid, governmentBalance);
    }

    public GovernmentFunds(JsonObject object) {
        this(
                JsonUtils.getJsonUUID(object, "government_uuid"),
                JsonUtils.getJsonBigDecimal(object, "government_balance")
        );
    }

    @Override
    public JsonObject toJson() throws IllegalStateException {
        JsonObject object = new JsonObject();
        object.addProperty("government_uuid", governmentUUID.toString());
        object.addProperty("government_balance", governmentBalance);
        return object;
    }

    public GovernmentFunds addBalance(BigDecimal amount) {
        return new GovernmentFunds(governmentUUID, governmentBalance.add(amount));
    }

    public GovernmentFunds subtractBalance(BigDecimal amount) {
        return new GovernmentFunds(governmentUUID, governmentBalance.subtract(amount));
    }
}
