package nl.radiantrealm.bankconomy.record.bank;

import nl.radiantrealm.bankconomy.Database;

import java.math.BigDecimal;
import java.util.UUID;

public record PlayerAccount(UUID playerUUID, BigDecimal playerBalance, String playerName) implements BankAccount {
    public static final AccountType ACCOUNT_TYPE = AccountType.PLAYER_ACCOUNT;
    public static final String TABLE_NAME = Database.TableName.PLAYER_ACCOUNTS.tableName;

    public static String COLUMN_NAME(ColumnName columnName) {
        return switch (columnName) {
            case ACCOUNT_UUID -> "player_uuid";
            case ACCOUNT_BALANCE -> "player_balance";
            case ACCOUNT_NAME -> "player_name";
        };
    }

    @Override
    public AccountType accountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public UUID accountUUID() {
        return playerUUID;
    }

    @Override
    public BigDecimal accountBalance() {
        return playerBalance;
    }

    @Override
    public String accountName() {
        return playerName;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String columnName(ColumnName columnName) {
        return COLUMN_NAME(columnName);
    }

    @Override
    public BankAccount updateBalance(BigDecimal accountBalance) {
        return new PlayerAccount(
                playerUUID,
                accountBalance,
                playerName
        );
    }

    @Override
    public BankAccount updateName(String accountName) {
        return new PlayerAccount(
                playerUUID,
                playerBalance,
                accountName
        );
    }
}
