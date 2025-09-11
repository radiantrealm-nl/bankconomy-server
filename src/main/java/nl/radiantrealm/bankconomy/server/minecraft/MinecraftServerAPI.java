package nl.radiantrealm.bankconomy.server.minecraft;

import nl.radiantrealm.bankconomy.server.minecraft.handler.MinecraftGenerateTokenAPI;
import nl.radiantrealm.bankconomy.server.minecraft.handler.MinecraftOnPlayerJoinAPI;
import nl.radiantrealm.bankconomy.server.minecraft.handler.MinecraftPlayerBalanceAPI;
import nl.radiantrealm.library.http.server.ApplicationRouter;

public class MinecraftServerAPI extends ApplicationRouter {

    public MinecraftServerAPI() {
        super(69420);

        register("/on-player-join", new MinecraftOnPlayerJoinAPI());
        register("/sync-player-balance", new MinecraftPlayerBalanceAPI());
        register("/generate-otp-token", new MinecraftGenerateTokenAPI());
    }
}
