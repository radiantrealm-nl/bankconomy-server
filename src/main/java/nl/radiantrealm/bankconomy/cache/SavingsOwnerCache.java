package nl.radiantrealm.bankconomy.cache;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.library.cache.CacheRegistry;
import nl.radiantrealm.library.utils.FormatUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SavingsOwnerCache extends CacheRegistry<UUID, List<UUID>> {

    public SavingsOwnerCache() {
        super(Duration.ofMinutes(15));
    }

    @Override
    protected List<UUID> load(UUID uuid) throws Exception {
        List<UUID> list = new ArrayList<>();

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT savings_uuid FROM bankconomy_savings WHERE owner_uuid = ?"
            );

            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                list.add(FormatUtils.formatUUID(rs, "savings_uuid"));
            }
        }

        return list;
    }
}
