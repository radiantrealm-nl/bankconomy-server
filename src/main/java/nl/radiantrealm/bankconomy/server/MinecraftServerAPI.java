package nl.radiantrealm.bankconomy.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
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
    }

    public class OnPlayerJoin implements RequestHandler {

        @Override
        public void handle(HttpRequest request) throws Exception {
            JsonObject object = JsonUtils.getJsonObject(request.getRequestBody());

            UUID playerUUID = JsonUtils.getJsonUUID(object, "player_uuid");
            String playerName = JsonUtils.getJsonString(object, "player_name");

            PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

            JsonObject response = new JsonObject();

            if (playerAccount == null) {
                response.addProperty("player_balance", BigDecimal.ZERO);
                response.addProperty("new_account", true);

                ProcessHandler handler = new PlayerAccountOperations.CreateAccount(playerUUID, playerName);
                Processor.createProcess(handler, object, null);
            } else {
                response.addProperty("player_balance", playerAccount.playerBalance());
                response.addProperty("new_account", false);

                if (!playerAccount.playerName().equals(playerName)) {
                    ProcessHandler handler = new PlayerAccountOperations.UpdateName(playerUUID, playerName);
                    Processor.createProcess(handler, object, null);
                }
            }

            request.sendResponse(StatusCode.OK, response);
        }
    }

    public class SyncPlayerBalance implements RequestHandler {

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
}
