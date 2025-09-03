package nl.radiantrealm.bankconomy;

import nl.radiantrealm.bankconomy.cache.GovernmentFundsCache;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsAccountCache;

public class Main {
    public static final GovernmentFundsCache governmentFundsCache = new GovernmentFundsCache();
    public static final PlayerAccountCache playerAccountCache = new PlayerAccountCache();
    public static final SavingsAccountCache savingsAccountCache = new SavingsAccountCache();

    public static void main(String[] args) {
    }
}
