package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonObject;
import nl.radiantrealm.library.http.HttpRequest;

import java.util.UUID;

public interface PublicRequestHandler {
    void handle(HttpRequest request, UUID playerUUID, JsonObject object) throws Exception;
}
