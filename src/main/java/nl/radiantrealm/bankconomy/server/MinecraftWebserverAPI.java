package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.cache.SessionTokenCache;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.http.server.ApplicationRouter;
import nl.radiantrealm.library.utils.FormatUtils;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.net.HttpCookie;
import java.util.UUID;

public class MinecraftWebserverAPI extends ApplicationRouter {

    public MinecraftWebserverAPI() {
        super(69420);

        registerPublic("/create-process", new PublicCreateProcessAPI());
        registerPublic("/get-dto", new PublicDataObjectAPI());
        registerPublic("/get-view", new PublicJsonViewAPI());
    }

    private void registerPublic(String path, PublicRequestHandler handler) {
        register(path, request -> {
            HttpCookie cookie = request.getCookie("csrf");

            if (cookie == null) {
                request.sendStatusResponse(StatusCode.UNAUTHORIZED);
                return;
            }

            UUID sessionUUID = Result.nullFunction(() -> FormatUtils.formatUUID(cookie.getValue()));

            if (sessionUUID == null) {
                request.sendStatusResponse(StatusCode.UNAUTHORIZED);
                return;
            }

            UUID playerUUID = SessionTokenCache.verifySessionUUID(sessionUUID);

            if (playerUUID == null) {
                request.sendStatusResponse(StatusCode.UNAUTHORIZED);
                return;
            }

            JsonObject object = Result.nullFunction(request::getRequestBodyAsJson);

            if (object == null) {
                request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing JSON body.");
                return;
            }

            handler.handle(request, playerUUID, object);
        });
    }
}
