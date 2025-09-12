package nl.radiantrealm.bankconomy.processor.operations;

import com.google.gson.JsonObject;
import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.bankconomy.Main;
import nl.radiantrealm.bankconomy.cache.GovernmentFundsCache;
import nl.radiantrealm.bankconomy.cache.PlayerAccountCache;
import nl.radiantrealm.bankconomy.enumerator.GovernmentUUID;
import nl.radiantrealm.bankconomy.enumerator.TransactionType;
import nl.radiantrealm.bankconomy.record.GovernmentFunds;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.bankconomy.record.Transaction;
import nl.radiantrealm.library.processor.Process;
import nl.radiantrealm.library.processor.ProcessHandler;
import nl.radiantrealm.library.processor.ProcessResult;
import nl.radiantrealm.library.utils.JsonUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.UUID;

public record ShopTransactionOperations(TransactionType transactionType, BigDecimal transactionAmount, UUID playerUUID, String message) implements ProcessHandler {
    private static final PlayerAccountCache playerAccountCache = Main.playerAccountCache;
    private static final GovernmentFundsCache governmentFundsCache = Main.governmentFundsCache;

    public ShopTransactionOperations(JsonObject object) throws Exception {
        this(
                JsonUtils.getJsonEnum(object, "transaction_type", TransactionType.class),
                JsonUtils.getJsonBigDecimal(object, "transaction_amount"),
                JsonUtils.getJsonUUID(object, "player_uuid"),
                JsonUtils.getJsonString(object, "message")
        );
    }

    @Override
    public ProcessResult handle(Process process) throws Exception {
        switch (transactionType) {
            case SHOP_BUY_PRODUCT,
                 SHOP_SELL_PRODUCT -> {}

            default -> ProcessResult.error(422, "Unsupported transaction type.");
        }

        PlayerAccount playerAccount = playerAccountCache.get(playerUUID);

        if (playerAccount == null) {
            return ProcessResult.error(404, "No player account found.");
        }

        GovernmentFunds governmentFunds = governmentFundsCache.get(GovernmentUUID.GOVERNMENT_FUND);

        if (governmentFunds == null) {
            return ProcessResult.error(500, "Server error.");
        }

        if (transactionType.equals(TransactionType.SHOP_BUY_PRODUCT) && !playerAccount.hasSufficientBalance(transactionAmount)) {
            return ProcessResult.error(422, "Insufficient balance.");
        }

        switch (transactionType) {
            case SHOP_BUY_PRODUCT -> {
                playerAccount = playerAccount.subtractBalance(transactionAmount);
                governmentFunds = governmentFunds.addBalance(transactionAmount);
            }

            case SHOP_SELL_PRODUCT -> {
                playerAccount = playerAccount.addBalance(transactionAmount);
                governmentFunds = governmentFunds.subtractBalance(transactionAmount);
            }
        }

        Transaction transaction = new Transaction(
                transactionType,
                transactionAmount,
                transactionType.equals(TransactionType.SHOP_BUY_PRODUCT) ? playerUUID : GovernmentUUID.GOVERNMENT_FUND.uuid,
                transactionType.equals(TransactionType.SHOP_SELL_PRODUCT) ? playerUUID : GovernmentUUID.GOVERNMENT_FUND.uuid,
                message
        );

        Connection connection = Database.getConnection(false);

        try (connection) {
            Database.updatePlayerBalance(connection, playerAccount);
            Database.updateGovernmentFunds(connection, governmentFunds);
            Database.insertTransactionLog(connection, transaction.createTransactionLog());
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            return ProcessResult.error(500, "Database error.", e);
        }

        playerAccountCache.put(playerAccount);
        governmentFundsCache.put(governmentFunds);
        return ProcessResult.ok();
    }
}
