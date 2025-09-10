package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SessionTokenCache;
import nl.radiantrealm.bankconomy.processor.Processor;
import nl.radiantrealm.bankconomy.processor.operations.PlayerAccountOperations;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.http.server.ApplicationRouter;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.utils.FormatUtils;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MinecraftServerAPI extends ApplicationRouter {
    private final PlayerAccountCache playerAccountCache = Main.playerAccountCache;

    public MinecraftServerAPI() {
        super(69420);

        register("/on-player-join", new OnPlayerJoin());
        register("/sync-player-balance", new SyncPlayerBalance());
        register("/generate-otp-token", new GetOTPToken());
    }

    private class OnPlayerJoin implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            JsonObject object = JsonUtils.getJsonObject(request.getRequestBody());

            UUID playerUUID = JsonUtils.getJsonUUID(object, "player_uuid");
            String playerName = JsonUtils.getJsonString(object, "player_name");

            PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

            if (playerAccount == null) {
                sendResponse(request, BigDecimal.ZERO, true);
                ProcessHandler handler = new PlayerAccountOperations.CreateAccount(playerUUID, playerName);
                Processor.createProcess(handler, object, null);
            } else {
                sendResponse(request, playerAccount.playerBalance(), false);

                if (!playerAccount.playerName().equals(playerName)) {
                    ProcessHandler handler = new PlayerAccountOperations.UpdateName(playerUUID, playerName);
                    Processor.createProcess(handler, object, null);
                }
            }
        }

        private void sendResponse(HttpRequest request, BigDecimal playerBalance, boolean newAccount) throws Exception {
            JsonObject object = new JsonObject();
            object.addProperty("player_balance", playerBalance);
            object.addProperty("new_account", newAccount);
            request.sendResponse(StatusCode.OK, object);
        }
    }

    private class SyncPlayerBalance implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            JsonObject object = JsonUtils.getJsonObject(request.getRequestBody());

            JsonArray array = JsonUtils.getJsonArray(object, "player_uuids");
            List<UUID> list = new ArrayList<>();

            for (JsonElement element : array) {
                list.add(FormatUtils.formatUUID(element.getAsString()));
            }

            Map<UUID, PlayerAccount> map = playerAccountCache.get(list);
            JsonObject response = new JsonObject();

            for (PlayerAccount playerAccount : map.values()) {
                response.addProperty(
                        playerAccount.playerUUID().toString(),
                        playerAccount.playerBalance()
                );
            }

            request.sendResponse(StatusCode.OK, response);
        }
    }

    private static class GetOTPToken implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            JsonObject object = JsonUtils.getJsonObject(request.getRequestBody());

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
}
