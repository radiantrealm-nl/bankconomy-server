package nl.radiantrealm.bankconomy.record.bank;

import nl.radiantrealm.library.utils.json.JsonConvertible;
import nl.radiantrealm.library.utils.json.JsonObject;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankAccount extends JsonConvertible {
    AccountType accountType();
    UUID accountUUID();
    BigDecimal accountBalance();
    String accountName();

    enum AccountType {
        PLAYER_ACCOUNT,
        SAVINGS_ACCOUNT,
        FUNDINGS_ACCOUNT,
    }

    enum ColumnName {
        ACCOUNT_UUID,
        ACCOUNT_BALANCE,
        ACCOUNT_NAME,
    }

    String tableName();
    String columnName(ColumnName columnName);

    @Override
    default JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.add(columnName(ColumnName.ACCOUNT_UUID), accountUUID());
        object.add(columnName(ColumnName.ACCOUNT_BALANCE), accountBalance());
        object.add(columnName(ColumnName.ACCOUNT_NAME), accountName());
        return object;
    }

    BankAccount updateBalance(BigDecimal accountBalance);
    BankAccount updateName(String accountName);

    default BankAccount addBalance(BigDecimal amount) {
        return updateBalance(accountBalance().add(amount));
    }

    default BankAccount subtractBalance(BigDecimal amount) {
        return updateBalance(accountBalance().subtract(amount));
    }

    default boolean hasSufficientBalance(BigDecimal amount) {
        return accountBalance().compareTo(amount) >= 0;
    }
}
