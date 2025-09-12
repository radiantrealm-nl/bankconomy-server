package nl.radiantrealm.bankconomy.server.internal;

import nl.radiantrealm.bankconomy.server.internal.handler.InternalCreateProcessAPI;
import nl.radiantrealm.library.http.server.ApplicationRouter;

public class InternalServerAPI extends ApplicationRouter {

    public InternalServerAPI() {
        super(69420);

        register("/create-process", new InternalCreateProcessAPI());
    }
}
