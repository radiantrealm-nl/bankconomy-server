package nl.radiantrealm.bankconomy.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.library.cache.CacheRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonViewBuilderCache {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final SavingsAccountCache savingsAccountCache = Main.savingsAccountCache;
    private static final SavingsOwnerCache savingsOwnerCache = Main.savingsOwnerCache;

    public static final SavingsAccountsList savingsAccountsList = new SavingsAccountsList();
    public static final SavingsAccountDetailed savingsAccountDetailed = new SavingsAccountDetailed();
    public static final RecentSavingsTransactions recentSavingsTransactions = new RecentSavingsTransactions();

    public JsonViewBuilderCache() {}

    public static class SavingsAccountsList extends CacheRegistry<UUID, JsonArray> {

        public SavingsAccountsList() {
            super(Duration.ofMinutes(5));
        }

        @Override
        protected JsonArray load(UUID uuid) throws Exception {
            Map<UUID, SavingsAccount> savingsAccountMap = savingsAccountCache.get(savingsOwnerCache.get(uuid));

            JsonArray array = new JsonArray();

            for (SavingsAccount savingsAccount : savingsAccountMap.values()) {
                JsonObject object = new JsonObject();
                object.addProperty("savings_uuid", savingsAccount.savingsUUID().toString());
                object.addProperty("savings_balance", savingsAccount.savingsBalance().toString());
                object.addProperty("savings_name", savingsAccount.savingsName());
                array.add(object);
            }

            return array;
        }
    }

    public static class SavingsAccountDetailed extends CacheRegistry<UUID, JsonObject> {

        public SavingsAccountDetailed() {
            super(Duration.ofMinutes(5));
        }

        @Override
        protected JsonObject load(UUID uuid) throws Exception {
            JsonObject object = savingsAccountCache.get(uuid).toJson();

            try (Connection connection = Database.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT `timestamp` FROM bankconomy_savings WHERE related_uuid = ? AND audit_type = 'CREATE_SAVINGS_ACCOUNT'"
                );

                statement.setString(1, uuid.toString());
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    object.addProperty("created_on", rs.getLong("timestamp"));
                } else {
                    object.addProperty("created_on", 0);
                }
            }

            return object;
        }
    }

    public static class RecentSavingsTransactions extends CacheRegistry<UUID, JsonObject> {

        public RecentSavingsTransactions() {
            super(Duration.ofMinutes(5));
        }

        @Override
        protected JsonObject load(UUID uuid) throws Exception {
            Map<Integer, JsonObject> map = new HashMap<>();

            try (Connection connection = Database.getConnection()) {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM bankconomy_transactions WHERE source_uuid = ? OR offset_uuid = ? ORDER BY log_id DESC LIMIT 4"
                );

                statement.setString(1, uuid.toString());
                statement.setString(2, uuid.toString());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    int logID = rs.getInt("log_id");

                    JsonObject object = new JsonObject();
                    object.addProperty("log_id", logID);
                    object.addProperty("transaction_type", rs.getString("transaction_type"));
                    object.addProperty("transaction_amount", rs.getString("transaction_amount"));
                    object.addProperty("message", rs.getString("message"));
                    map.put(logID, object);
                }
            }

            JsonObject object = new JsonObject();
            map.forEach((key, value) -> object.add(key.toString(), value));
            return object;
        }
    }
}
