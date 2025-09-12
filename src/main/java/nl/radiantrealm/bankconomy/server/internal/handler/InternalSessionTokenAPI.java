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

        UUID sessionUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(requestBody, "session_uuid"));

        if (sessionUUID == null) {
            request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing session UUID.");
            return;
        }

        UUID playerUUID = SessionTokenCache.verifySessionUUID(sessionUUID);

        if (playerUUID == null) {
            request.sendStatusResponse(StatusCode.NOT_FOUND, "No player found.");
            return;
        }

        JsonObject responseBody = new JsonObject();
        responseBody.addProperty("player_uuid", sessionUUID.toString());
        request.sendResponse(StatusCode.OK, responseBody);
    }
}
