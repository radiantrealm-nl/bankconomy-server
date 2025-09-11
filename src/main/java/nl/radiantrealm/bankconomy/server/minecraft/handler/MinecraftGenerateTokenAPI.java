package nl.radiantrealm.bankconomy.server.minecraft.handler;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.cache.SessionTokenCache;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.JsonUtils;

import java.util.UUID;

public class MinecraftGenerateTokenAPI implements RequestHandler {

    @Override
    public void handle(HttpRequest request) throws Exception {
        JsonObject object = request.getRequestBodyAsJson();

        UUID playerUUID = JsonUtils.getJsonUUID(object, "player_uuid");
        Integer token = SessionTokenCache.generateOTPToken(playerUUID);

        if (token == null) {
            request.sendStatusResponse(StatusCode.SERVER_ERROR, "Failed to generate OTP Token.");
        } else {
            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("token", token);
            request.sendResponse(StatusCode.OK, responseBody);
        }
    }
}
