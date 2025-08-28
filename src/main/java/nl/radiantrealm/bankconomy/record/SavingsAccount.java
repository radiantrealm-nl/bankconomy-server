package nl.radiantrealm.bankconomy.record;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.utils.DataObject;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsAccount(UUID savingsUUID, UUID ownerUUID, BigDecimal savingsBalance, BigDecimal accumulatedInterest, String savingsName) implements DataObject {

    public SavingsAccount(JsonObject object) throws IllegalArgumentException {
        this(
                JsonUtils.getJsonUUID(object, "savings_uuid"),
                JsonUtils.getJsonUUID(object, "owner_uuid"),
                JsonUtils.getJsonBigDecimal(object, "savings_balance"),
                JsonUtils.getJsonBigDecimal(object, "accumulated_interest"),
                JsonUtils.getJsonString(object, "savings_name")
        );
    }

    @Override
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("savings_uuid", savingsUUID.toString());
        object.addProperty("owner_uuid", ownerUUID.toString());
        object.addProperty("savings_balance", savingsBalance);
        object.addProperty("accumulated_interest", accumulatedInterest);
        object.addProperty("savings_name", savingsName);
        return object;
    }

    public SavingsAccount addBalance(BigDecimal amount) {
        return new SavingsAccount(savingsUUID, ownerUUID, savingsBalance.add(amount), accumulatedInterest, savingsName);
    }

    public SavingsAccount subtractBalance(BigDecimal amount) {
        return new SavingsAccount(savingsUUID, ownerUUID, savingsBalance.subtract(amount), accumulatedInterest, savingsName);
    }

    public SavingsAccount accumulateInterest(BigDecimal amount) {
        return new SavingsAccount(savingsUUID, ownerUUID, savingsBalance, accumulatedInterest.add(amount), savingsName);
    }

    public SavingsAccount payoutInterest(BigDecimal amount) {
        return new SavingsAccount(savingsUUID, ownerUUID, savingsBalance.add(amount), accumulatedInterest.subtract(amount), savingsName);
    }

    public SavingsAccount updateName(String savingsName) {
        return new SavingsAccount(savingsUUID, ownerUUID, savingsBalance, accumulatedInterest, savingsName);
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return savingsBalance.compareTo(amount) >= 0;
    }
}
