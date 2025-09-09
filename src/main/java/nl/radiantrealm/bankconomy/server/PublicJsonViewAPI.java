package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.cache.JsonViewBuilderCache;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.Map;
import java.util.UUID;

public class PublicJsonViewAPI implements PublicRequestHandler {

    @Override
    public void handle(HttpRequest request, UUID playerUUID, JsonObject object) throws Exception {
        JsonObject requestedViews = Result.nullFunction(() -> JsonUtils.getJsonObject(object, "views"));

        if (requestedViews == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing views body.");
            return;
        }

        JsonObject responseBody = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : requestedViews.entrySet()) {
            JsonObject params = Result.nullFunction(() -> entry.getValue().getAsJsonObject());

            if (params == null) {
                responseBody.add(entry.getKey(), addJsonError(String.format("Missing JSON body for '%s'.", entry.getKey())));
                continue;
            }

            responseBody.add(entry.getKey(), switch (entry.getKey()) {
                case "savings_accounts_list" -> JsonViewBuilderCache.savingsAccountsList.get(playerUUID);

                case "savings_account_detailed" -> {
                    UUID savingsUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(params, "savings_uuid"));

                    if (savingsUUID == null) {
                        yield addJsonError("Missing savings UUID.");
                    }

                    yield JsonViewBuilderCache.savingsAccountDetailed.get(savingsUUID);
                }

                case "recent_savings_transactions" -> {
                    UUID savingsUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(params, "savings_uuid"));

                    if (savingsUUID == null) {
                        yield addJsonError("Missing savings UUID.");
                    }

                    yield JsonViewBuilderCache.recentSavingsTransactions.get(savingsUUID);
                }

                default -> addJsonError("Unknown view type.");
            });
        }

        request.sendStatusResponse(StatusCode.OK, responseBody.getAsString());
    }

    private JsonObject addJsonError(String error) {
        JsonObject object = new JsonObject();
        object.addProperty("error", error);
        return object;
    }
}
