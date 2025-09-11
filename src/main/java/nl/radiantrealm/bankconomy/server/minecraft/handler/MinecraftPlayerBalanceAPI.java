package nl.radiantrealm.bankconomy.server.minecraft.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.library.http.HttpRequest;
import nl.radiantrealm.library.http.RequestHandler;
import nl.radiantrealm.library.http.StatusCode;
import nl.radiantrealm.library.utils.FormatUtils;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MinecraftPlayerBalanceAPI implements RequestHandler {

    @Override
    public void handle(HttpRequest request) throws Exception {
        JsonObject object = request.getRequestBodyAsJson();

        JsonArray array = JsonUtils.getJsonArray(object, "player_uuids");
        List<UUID> list = new ArrayList<>(array.size());

        for (JsonElement element : array) {
            UUID playerUUID = FormatUtils.formatUUID(element.getAsString());
            list.add(playerUUID);
        }

        Map<UUID, BigDecimal> result = new HashMap<>(list.size());

        try (Connection connection = Database.getConnection()) {
            String params = String.join(", ", Collections.nCopies(list.size(), "?"));

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT player_uuid, player_balance FROM bankconomy_players WHERE player_uuid IN (" + params + ")"
            );

            for (int i = 0; i < list.size(); i++) {
                statement.setString(i + 1, list.get(i).toString());
            }

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                result.put(
                        FormatUtils.formatUUID(rs, "player_uuid"),
                        rs.getBigDecimal("player_balance")
                );
            }
        }

        JsonObject responseBody = new JsonObject();

        for (Map.Entry<UUID, BigDecimal> entry : result.entrySet()) {
            responseBody.addProperty(entry.getKey().toString(), entry.getValue());
        }

        request.sendResponse(StatusCode.OK, responseBody);
    }
}
