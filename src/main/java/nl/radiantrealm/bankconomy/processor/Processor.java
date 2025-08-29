package nl.radiantrealm.bankconomy.processor;

import nl.radiantrealm.bankconomy.processor.operations.InterestAccumulationOperation;
import nl.radiantrealm.bankconomy.processor.operations.InterestPayoutOperation;
import nl.radiantrealm.bankconomy.processor.operations.SavingsAccountOperations;
import nl.radiantrealm.bankconomy.processor.operations.TransactionOperations;
import nl.radiantrealm.library.processor.ProcessRouter;

public class Processor extends ProcessRouter {

    public Processor() {
        super(100);

        registerHandler("/create-savings-account", new SavingsAccountOperations.CreateAccount());
        registerHandler("/update-savings-name", new SavingsAccountOperations.UpdateName());
        registerHandler("/delete-savings-account", new SavingsAccountOperations.DeleteAccount());
        registerHandler("/create-transaction", new TransactionOperations());
        registerHandler("/interest-accumulation", new InterestAccumulationOperation());
        registerHandler("/interest-payout", new InterestPayoutOperation());
    }
}
