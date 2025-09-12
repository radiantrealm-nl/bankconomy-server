package nl.radiantrealm.bankconomy.server.internal.handler;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.cache.SessionTokenCache;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.UUID;

public class InternalSessionTokenAPI implements RequestHandler {

    @Override
    public void handle(HttpRequest request) throws Exception {
        JsonObject requestBody = Result.nullFunction(request::getRequestBodyAsJson);

        if (requestBody == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing JSON body.");
            return;
        }

        UUID playerUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(requestBody, "player_uuid"));

        if (playerUUID == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing player UUID.");
            return;
        }

        UUID sessionUUID = SessionTokenCache.verifyPlayerUUID(playerUUID);

        if (sessionUUID == null) {
            request.sendStatusResponse(StatusCode.NOT_FOUND, "No session token found.");
            return;
        }

        JsonObject responseBody = new JsonObject();
        responseBody.addProperty("session_uuid", sessionUUID.toString());
        request.sendResponse(StatusCode.OK, responseBody);
    }
}
