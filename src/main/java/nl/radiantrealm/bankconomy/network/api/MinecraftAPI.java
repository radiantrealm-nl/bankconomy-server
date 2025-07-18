package nl.radiantrealm.bankconomy.network.api;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.controller.Cache;
import nl.radiantrealm.bankconomy.controller.Processor;
import nl.radiantrealm.bankconomy.processor.CreatePlayerAccount;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.server.Request;
import nl.radiantrealm.library.server.RequestHandler;
import nl.radiantrealm.library.server.Response;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class MinecraftAPI {

    public class OnPlayerJoinHandler extends RequestHandler {

        public OnPlayerJoinHandler() {
            super(true);
        }

        @Override
        protected Response handle(Request request) throws Exception {
            Result<UUID> playerUUID = JsonUtils.getJsonUUID(request.getBody(), "uuid");

            if (playerUUID.isObjectEmpty()) {
                return Response.error(400, "Missing required `uuid` field.");
            }

            Result<String > playerName = JsonUtils.getJsonString(request.getBody(), "name");

            if (playerName.isObjectEmpty()) {
                return Response.error(400, "Missing required `name` field.");
            }

            Result<PlayerAccount> playerAccountResult = Cache.playerAccountCache.get(playerUUID.getObject());

            if (!playerAccountResult.isObjectEmpty()) {
                JsonObject object = new JsonObject();
                object.addProperty("balance", playerAccountResult.getObject().playerBalance().toString());
                return new Response(200, Optional.of(object));
            }

            AtomicReference<ProcessResult<Void>> reference = new AtomicReference<>();

            if (!Processor.createProcess(new CreatePlayerAccount(playerUUID.getObject(), playerName.getObject()), reference::set)) {
                return Response.error(500, "Server error.");
            }

            if (reference.get().throwable().isEmpty()) { //Add success bool devs pleaseeee
                return Response.ok();
            } else {
                return Response.error(500, "Server error.");
            }
        }
    }
}
