package nl.radiantrealm.bankconomy.network.api;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.controller.Cache;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.server.Request;
import nl.radiantrealm.library.server.RequestHandler;
import nl.radiantrealm.library.server.Response;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Parsable;

import java.util.Optional;
import java.util.UUID;

public class MinecraftAPI {

    public class OnPlayerJoinHandler extends RequestHandler {

        public OnPlayerJoinHandler() {
            super(true);
        }

        @Override
        protected Response handle(Request request) throws Exception {
            Parsable<UUID> playerUUID = JsonUtils.getJsonUUID(request.getBody(), "uuid");

            if (playerUUID.isObjectEmpty()) {
                return new Response(
                        400,
                        Optional.of(new JsonObject()) //Add method to directly use Response.error(int, error message) for faster access!
                );
            }

            Parsable<String> playerName = JsonUtils.getJsonString(request.getBody(), "name");

            if (playerName.isObjectEmpty()) {
                return new Response(
                        400,
                        Optional.of(new JsonObject())
                );
            }

            Optional<PlayerAccount> playerAccount = Cache.playerAccountCache.get(playerUUID.getObject());
        }
    }
}
