package nl.radiantrealm.bankconomy.processor;

import nl.radiantrealm.bankconomy.processor.operations.PlayerAccountOperations;
import nl.radiantrealm.bankconomy.processor.operations.SavingsAccountOperations;
import nl.radiantrealm.bankconomy.processor.operations.TransactionOperations;
import nl.radiantrealm.library.processor.ProcessHandler;

public enum ProcessType {
    CREATE_PLAYER_ACCOUNT(new PlayerAccountOperations.CreateAccount()),
    UPDATE_PLAYER_NAME(new PlayerAccountOperations.UpdateName()),

    CREATE_SAVINGS_ACCOUNT(new SavingsAccountOperations.CreateAccount()),
    UPDATE_SAVINGS_NAME(new SavingsAccountOperations.UpdateName()),
    DELETE_SAVINGS_ACCOUNT(new SavingsAccountOperations.DeleteAccount()),

    CREATE_TRANSACTION(new TransactionOperations()),

    ;

    public final ProcessHandler handler;

    ProcessType(ProcessHandler handler) {
        this.handler = handler;
    }
}
