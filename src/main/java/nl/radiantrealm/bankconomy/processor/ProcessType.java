package nl.radiantrealm.bankconomy.processor;

import nl.radiantrealm.bankconomy.processor.operations.PlayerAccountOperations;
import nl.radiantrealm.bankconomy.processor.operations.SavingsAccountOperations;
import nl.radiantrealm.bankconomy.processor.operations.TransactionOperations;
import nl.radiantrealm.library.processor.ProcessHandler;

public enum ProcessType {
    CREATE_PLAYER_ACCOUNT(PlayerAccountOperations.CreateAccount.class),
    UPDATE_PLAYER_NAME(PlayerAccountOperations.UpdateName.class),

    CREATE_SAVINGS_ACCOUNT(SavingsAccountOperations.CreateAccount.class),
    UPDATE_SAVINGS_NAME(SavingsAccountOperations.UpdateName.class),
    DELETE_SAVINGS_ACCOUNT(SavingsAccountOperations.DeleteAccount.class),

    CREATE_TRANSACTION(TransactionOperations.class);

    public final Class<? extends ProcessHandler> handler;

    ProcessType(Class<? extends ProcessHandler> handler) {
        this.handler = handler;
    }
}
