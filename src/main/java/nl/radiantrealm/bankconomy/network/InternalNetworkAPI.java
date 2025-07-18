package nl.radiantrealm.bankconomy.network;

import nl.radiantrealm.bankconomy.network.api.MinecraftAPI;
import nl.radiantrealm.library.server.ApplicationRouter;

public class InternalNetworkAPI extends ApplicationRouter {

    public InternalNetworkAPI() {
        super(26000);

        MinecraftAPI minecraftAPI = new MinecraftAPI();
        createAPIHandler("/minecraft/on-player-join", minecraftAPI.onPlayerJoinHandler);
        createAPIHandler("/minecraft/get-player-balance", minecraftAPI.getPlayerBalance);
    }
}
