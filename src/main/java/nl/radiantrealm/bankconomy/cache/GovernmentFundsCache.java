package nl.radiantrealm.bankconomy.cache;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.enumerator.GovernmentUUID;
import nl.radiantrealm.bankconomy.record.GovernmentFunds;
import nl.radiantrealm.library.cache.CacheRegistry;
import nl.radiantrealm.library.utils.FormatUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class GovernmentFundsCache extends CacheRegistry<UUID, GovernmentFunds> {

    public GovernmentFundsCache() {
        super(300000);
    }

    public GovernmentFunds get(GovernmentUUID governmentUUID) throws Exception {
        return get(governmentUUID.uuid);
    }

    @Override
    protected GovernmentFunds load(UUID uuid) throws Exception {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM bankconomy_government_funds WHERE government_uuid = ?"
            );

            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new GovernmentFunds(
                        FormatUtils.formatUUID(rs.getString("government_uuid")),
                        rs.getBigDecimal("government_balance")
                );
            }

            return null;
        }
    }

    @Override
    protected Map<UUID, GovernmentFunds> load(List<UUID> list) throws Exception {
        String params = String.join(", ", Collections.nCopies(list.size(), "?"));

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM bankconomy_government_funds WHERE government_uuid IN (" + params + ")"
            );

            for (int i = 0; i < list.size(); i++) {
                statement.setString(i + 1, list.get(i).toString());
            }

            ResultSet rs = statement.executeQuery();
            Map<UUID, GovernmentFunds> result = new HashMap<>(list.size());

            while (rs.next()) {
                UUID governmentUUID = FormatUtils.formatUUID(rs.getString("government_uuid"));

                result.put(governmentUUID, new GovernmentFunds(
                        governmentUUID,
                        rs.getBigDecimal("government_balance")
                ));
            }

            return result;
        }
    }
}
