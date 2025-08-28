package nl.radiantrealm.bankconomy;

import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsAccountCache;
import nl.radiantrealm.bankconomy.cache.SavingsOwnerCache;

public class Main {
    public static final PlayerAccountCache playerAccountCache = new PlayerAccountCache();
    public static final SavingsAccountCache savingsAccountCache = new SavingsAccountCache();
    public static final SavingsOwnerCache savingsOwnerCache = new SavingsOwnerCache();

    public static void main(String[] args) {
    }
}
