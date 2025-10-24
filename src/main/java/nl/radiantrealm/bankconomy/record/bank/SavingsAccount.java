package nl.radiantrealm.bankconomy.record.bank;

import nl.radiantrealm.bankconomy.Database;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.math.BigDecimal;
import java.util.UUID;

public record SavingsAccount(UUID savingsUUID, UUID ownerUUID, BigDecimal savingsBalance, BigDecimal accumulatedInterest, String savingsName) implements BankAccount {
    public static final AccountType ACCOUNT_TYPE = AccountType.SAVINGS_ACCOUNT;
    public static final String TABLE_NAME = Database.TableName.SAVINGS_ACCOUNTS.tableName;

    public static String COLUMN_NAME(ColumnName columnName) {
        return switch (columnName) {
            case ACCOUNT_UUID -> "savings_uuid";
            case ACCOUNT_BALANCE -> "savings_balance";
            case ACCOUNT_NAME -> "savings_name";
        };
    }

    @Override
    public AccountType accountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public UUID accountUUID() {
        return savingsUUID;
    }

    @Override
    public BigDecimal accountBalance() {
        return savingsBalance;
    }

    @Override
    public String accountName() {
        return savingsName;
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
    public JsonObject toJson() {
        JsonObject object = BankAccount.super.toJson();
        object.add("owner_uuid", ownerUUID);
        object.add("accumulated_interest", accumulatedInterest);
        return object;
    }

    @Override
    public BankAccount updateBalance(BigDecimal accountBalance) {
        return new SavingsAccount(
                savingsUUID,
                ownerUUID,
                accountBalance,
                accumulatedInterest,
                savingsName
        );
    }

    @Override
    public BankAccount updateName(String accountName) {
        return new SavingsAccount(
                savingsUUID,
                ownerUUID,
                savingsBalance,
                accumulatedInterest,
                accountName
        );
    }
}
