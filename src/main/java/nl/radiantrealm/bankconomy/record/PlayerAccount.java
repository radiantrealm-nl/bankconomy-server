package nl.radiantrealm.bankconomy.record;

import java.math.BigDecimal;
import java.util.UUID;

public record PlayerAccount(UUID playerUUID, BigDecimal playerBalance, String playerName) {}
