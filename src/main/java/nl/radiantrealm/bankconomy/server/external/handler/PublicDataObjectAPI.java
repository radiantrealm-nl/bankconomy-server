package nl.radiantrealm.bankconomy.server.external.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsAccountCache;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.bankconomy.server.external.PublicRequestHandler;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.Map;
import java.util.UUID;

public class PublicDataObjectAPI implements PublicRequestHandler {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final SavingsAccountCache savingsAccountCache = Main.savingsAccountCache;

    @Override
    public void handle(HttpRequest request, UUID playerUUID, JsonObject object) throws Exception {
        JsonObject requestedDTOs = Result.nullFunction(() -> JsonUtils.getJsonObject(object, "views"));

        if (requestedDTOs == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing dtos body.");
            return;
        }

        JsonObject responseBody = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : requestedDTOs.entrySet()) {
            JsonObject params = Result.nullFunction(() -> entry.getValue().getAsJsonObject());

            if (params == null) {
                responseBody.add(entry.getKey(), addJsonError(String.format("Missing JSON body for '%s'.", entry.getKey())));
                continue;
            }

            responseBody.add(entry.getKey(), switch (entry.getKey()) {
                case "player_account" -> playerAccountCache.get(playerUUID).toJson();

                case "savings_account" -> {
                    UUID savingsUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(params, "savings_uuid"));

                    if (savingsUUID == null) {
                        yield addJsonError("Missing savings UUID.");
                    }

                    SavingsAccount savingsAccount = savingsAccountCache.get(playerUUID);

                    if (!savingsAccount.ownerUUID().equals(playerUUID)) {
                        yield addJsonError("Insufficient permissions to view object.");
                    }

                    yield savingsAccount.toJson();
                }

                default -> addJsonError("Unknown dto type.");
            });
        }
    }

    private JsonObject addJsonError(String error) {
        JsonObject object = new JsonObject();
        object.addProperty("error", error);
        return object;
    }
}
