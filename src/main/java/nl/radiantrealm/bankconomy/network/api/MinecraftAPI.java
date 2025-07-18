package nl.radiantrealm.bankconomy.network.api;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.controller.Cache;
import nl.radiantrealm.bankconomy.controller.Processor;
import nl.radiantrealm.bankconomy.processor.CreatePlayerAccount;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.server.ApiRequest;
import nl.radiantrealm.library.server.ApiResponse;
import nl.radiantrealm.library.server.RequestHandler;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class MinecraftAPI {
    public final OnPlayerJoinHandler onPlayerJoinHandler;
    public final GetPlayerBalance getPlayerBalance;

    public MinecraftAPI() {
        this.onPlayerJoinHandler = new OnPlayerJoinHandler();
        this.getPlayerBalance = new GetPlayerBalance();
    }

    public static class OnPlayerJoinHandler extends RequestHandler {

        public OnPlayerJoinHandler() {
            super(true);
        }

        @Override
        protected ApiResponse handle(ApiRequest request) { //Remove the throws exception from handle devs
            Result<UUID> playerUUID = JsonUtils.getJsonUUID(request.getBody(), "uuid");

            if (playerUUID.isObjectEmpty()) {
                return ApiResponse.error(400, "Missing required `uuid` field.");
            }

            Result<String > playerName = JsonUtils.getJsonString(request.getBody(), "name");

            if (playerName.isObjectEmpty()) {
                return ApiResponse.error(400, "Missing required `name` field.");
            }

            Result<PlayerAccount> playerAccountResult = Cache.playerAccountCache.get(playerUUID.getObject());

            if (!playerAccountResult.isObjectEmpty()) {
                JsonObject object = new JsonObject();
                object.addProperty("balance", playerAccountResult.getObject().playerBalance().toString());
                return new ApiResponse(200, Optional.of(object));
            }

            AtomicReference<ProcessResult> reference = new AtomicReference<>();

            if (!Processor.createProcess(new CreatePlayerAccount(playerUUID.getObject(), playerName.getObject()), reference::set)) {
                return ApiResponse.error(500, "Server error.");
            }

            if (reference.get().throwable().isEmpty()) { //Add success bool devs pleaseeee
                return ApiResponse.ok();
            } else {
                return ApiResponse.error(500, "Server error.");
            }
        }
    }

    public static class GetPlayerBalance extends RequestHandler {

        public GetPlayerBalance() {
            super(true);
        }

        @Override
        protected ApiResponse handle(ApiRequest apiRequest) {
            Result<UUID> playerUUID = JsonUtils.getJsonUUID(apiRequest.getBody(), "uuid");

            if (playerUUID.isObjectEmpty()) {
                return ApiResponse.error(400, "Missing required `uuid` field.");
            }

            Result<PlayerAccount> playerAccount = Cache.playerAccountCache.get(playerUUID.getObject());

            if (playerAccount.isObjectEmpty()) {
                return ApiResponse.error(404, "Could not find player account.");
            }

            return ApiResponse.ok(Map.of("balance", playerAccount.getObject().playerBalance().toString()));
        }
    }
}
