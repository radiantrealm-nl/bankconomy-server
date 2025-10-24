package nl.radiantrealm.bankconomy.record.bank;

import nl.radiantrealm.bankconomy.Database;

import java.math.BigDecimal;
import java.util.UUID;

public record FundingsAccount(UUID fundingsUUID, BigDecimal fundingsBalance, String fundingsName) implements BankAccount {
    public static final AccountType ACCOUNT_TYPE = AccountType.FUNDINGS_ACCOUNT;
    public static final String TABLE_NAME = Database.TableName.FUNDINGS_ACCOUNTS.tableName;

    public static String COLUMN_NAME(ColumnName columnName) {
        return switch (columnName) {
            case ACCOUNT_UUID -> "fundings_uuid";
            case ACCOUNT_BALANCE -> "fundings_balance";
            case ACCOUNT_NAME -> "fundings_name";
        };
    }

    @Override
    public AccountType accountType() {
        return ACCOUNT_TYPE;
    }

    @Override
    public UUID accountUUID() {
        return fundingsUUID;
    }

    @Override
    public BigDecimal accountBalance() {
        return fundingsBalance;
    }

    @Override
    public String accountName() {
        return fundingsName;
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
        return new FundingsAccount(
                fundingsUUID,
                fundingsBalance,
                fundingsName
        );
    }

    @Override
    public BankAccount updateName(String accountName) {
        return new FundingsAccount(
                fundingsUUID,
                fundingsBalance,
                accountName
        );
    }
}
