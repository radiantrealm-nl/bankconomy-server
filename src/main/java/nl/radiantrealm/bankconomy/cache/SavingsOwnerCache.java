package nl.radiantrealm.bankconomy.cache;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.library.cache.CacheRegistry;
import nl.radiantrealm.library.utils.FormatUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class SavingsOwnerCache extends CacheRegistry<UUID, List<UUID>> {

    public SavingsOwnerCache() {
        super(3600000);
    }

    @Override
    protected List<UUID> load(UUID uuid) throws Exception {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT savings_uuid FROM bankconomy_savings WHERE owner_uuid = ?"
            );

            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            List<UUID> list = new ArrayList<>();

            while (rs.next()) {
                list.add(FormatUtils.formatUUID(rs.getString("savings_uuid")));
            }

            return list;
        }
    }

    @Override
    protected Map<UUID, List<UUID>> load(List<UUID> list) throws Exception {
        String params = String.join(", ", Collections.nCopies(list.size(), "?"));

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT savings_uuid, owner_uuid FROM bankconomy_savings WHERE savings_uuid IN (" + params + ")"
            );

            for (int i = 0; i < list.size(); i++) {
                statement.setString(i + 1, list.get(i).toString());
            }

            ResultSet rs = statement.executeQuery();
            Map<UUID, List<UUID>> result = new HashMap<>(list.size());

            while (rs.next()) {
                UUID ownerUUID = FormatUtils.formatUUID(rs.getString("owner_uuid"));
                UUID savingsUUID = FormatUtils.formatUUID(rs.getString("savings_uuid"));

                result.computeIfAbsent(ownerUUID, key -> new ArrayList<>()).add(savingsUUID);
            }

            return result;
        }
    }
}
