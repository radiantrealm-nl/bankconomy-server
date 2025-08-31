package nl.radiantrealm.bankconomy.record;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionLog(int logID, long timestamp, TransactionType transactionType, BigDecimal transactionAmount, UUID sourceUUID, UUID offsetUUID, String message) implements DataObject {

    public TransactionLog(JsonObject object) {
        this(
                JsonUtils.getJsonInteger(object, "log_id"),
                JsonUtils.getJsonLong(object, "timestamp"),
                JsonUtils.getJsonEnum(object, "transaction_type", TransactionType.class),
                JsonUtils.getJsonBigDecimal(object, "transaction_amount"),
                JsonUtils.getJsonUUID(object, "source_uuid"),
                JsonUtils.getJsonUUID(object, "offset_uuid"),
                JsonUtils.getJsonString(object, "message")
        );
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("log_id", logID);
        object.addProperty("timestamp", timestamp);
        object.addProperty("transaction_type", transactionType.name());
        object.addProperty("transaction_amount", transactionAmount);
        object.addProperty("source_uuid", sourceUUID.toString());
        object.addProperty("offset_uuid", offsetUUID.toString());
        object.addProperty("message", message);
        return object;
    }
}
