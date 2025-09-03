package nl.radiantrealm.bankconomy.record;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record Transaction(TransactionType transactionType, BigDecimal transactionAmount, UUID sourceUUID, UUID offsetUUID, String message) implements DataObject {

    public Transaction(JsonObject object) throws IllegalArgumentException {
        this(
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
        object.addProperty("transaction_type", transactionType.name());
        object.addProperty("transaction_amount", transactionAmount);
        object.addProperty("source_uuid", sourceUUID.toString());
        object.addProperty("offset_uuid", offsetUUID.toString());
        object.addProperty("message", message);
        return object;
    }

    public TransactionLog createTransactionLog() {
        return new TransactionLog(
                0,
                System.currentTimeMillis(),
                transactionType,
                transactionAmount,
                sourceUUID,
                offsetUUID,
                message
        );
    }

    public boolean isValidAmount(int decimals, boolean aboveZero) {
        if (aboveZero) {
            if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }

        if (transactionAmount.scale() > decimals) {
            return false;
        }

        return true;
    }
}
