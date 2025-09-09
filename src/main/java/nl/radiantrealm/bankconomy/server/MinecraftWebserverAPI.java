package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.http.server.ApplicationRouter;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.UUID;

public class MinecraftWebserverAPI extends ApplicationRouter {

    public MinecraftWebserverAPI() {
        super(69420);

        registerPublic("/create-process", new PublicCreateProcessAPI());
        registerPublic("/get-dto", new PublicDataObjectAPI());
        registerPublic("/get-view", new PublicJsonViewAPI());
    }

    private void registerPublic(String path, PublicRequestHandler handler) {
        server.createContext(path, exchange -> {
            try (exchange) {
                HttpRequest request = new HttpRequest(exchange);

                UUID playerUUID = UUID.randomUUID(); //Add auth later

                JsonObject object = Result.nullFunction(() -> JsonUtils.getJsonObject(request.getRequestBody()));

                if (object == null) {
                    request.sendStatusResponse(StatusCode.BAD_REQUEST, "Missing JSON body.");
                    return;
                }

                handler.handle(request, playerUUID, object);
            } catch (Exception e) {
                logger.error(String.format("Unexpected exception in %s.", handler.getClass().getSimpleName()), e);
            }
        });
    }
}
