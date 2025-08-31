package nl.radiantrealm.bankconomy.enumerator;

import java.util.UUID;

public enum GovernmentUUID {
    GOVERNMENT_FUND(UUID.fromString("e7db2d4f-9efb-5882-b38f-665be45ebbdc"));

    public static final UUID nameSpacedUUID = UUID.fromString("755b0c69-2501-4e07-910b-7568532cb525");

    public final UUID uuid;

    GovernmentUUID(UUID uuid) {
        this.uuid = uuid;
    }
}
