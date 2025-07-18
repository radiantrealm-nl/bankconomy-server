package nl.radiantrealm.bankconomy;

import nl.radiantrealm.bankconomy.controller.Cache;
import nl.radiantrealm.bankconomy.network.InternalNetworkAPI;

public class Main {
    public static void main(String[] args) {
        new Cache();
        new InternalNetworkAPI();
    }
}
