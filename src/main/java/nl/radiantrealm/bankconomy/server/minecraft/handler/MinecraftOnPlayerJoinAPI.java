package nl.radiantrealm.bankconomy.server.minecraft.handler;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.processor.Processor;
import nl.radiantrealm.bankconomy.processor.operations.PlayerAccountOperations;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.utils.JsonUtils;
import nl.radiantrealm.library.utils.Result;

import java.math.BigDecimal;
import java.util.UUID;

public class MinecraftOnPlayerJoinAPI implements RequestHandler {
    private final PlayerAccountCache playerAccountCache = Main.playerAccountCache;

    @Override
    public void handle(HttpRequest request) throws Exception {
        JsonObject object = Result.nullFunction(request::getRequestBodyAsJson);

        UUID playerUUID = Result.nullFunction(() -> JsonUtils.getJsonUUID(object, "player_uuid"));
        String playerName = Result.nullFunction(() -> JsonUtils.getJsonString(object, "player_name"));

        PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

        if (playerAccount == null) {
            sendRequest(request, BigDecimal.ZERO, true);
            ProcessHandler handler = new PlayerAccountOperations.CreateAccount(playerUUID, playerName);
            Processor.createProcess(handler);
        } else {
            sendRequest(request, playerAccount.playerBalance(), false);

            if (!playerAccount.playerName().equals(playerName)) {
                ProcessHandler handler = new PlayerAccountOperations.UpdateName(playerUUID, playerName);
                Processor.createProcess(handler);
            }
        }
    }

    private void sendRequest(HttpRequest request, BigDecimal playerBalancce, boolean newAccount) throws Exception {
        JsonObject object = new JsonObject();
        object.addProperty("player_balance", playerBalancce);
        object.addProperty("new_account", newAccount);
        request.sendResponse(StatusCode.OK, object);
    }
}
