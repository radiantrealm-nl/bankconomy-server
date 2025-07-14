package nl.radiantrealm.bankconomy.record;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsAccount(UUID savingsUUID, UUID ownerUUID, BigDecimal savingsBalance, String savingsName) {}
