package nl.radiantrealm.bankconomy;

import nl.radiantrealm.bankconomy.record.AuditLog;
import nl.radiantrealm.bankconomy.record.PlayerAccount;
import nl.radiantrealm.bankconomy.record.SavingsAccount;
import nl.radiantrealm.bankconomy.record.TransactionLog;
import nl.radiantrealm.library.controller.DatabaseController;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Database extends DatabaseController {

    @Override
    protected String setDatabaseURL() {
        return "localhost";
    }

    @Override
    protected String setDatabaseUsername() {
        return System.getenv("DB_USERNAME");
    }

    @Override
    protected String setDatabasePassword() {
        return System.getenv("DB_PASSWORD");
    }

    public static void insertTransactionLog(Connection connection, TransactionLog transactionLog) throws Exception {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bankconomy_transactions (`timestamp`, transaction_type, transaction_amount, source_uuid, offset_uuid, message) VALUES (?, ?, ?, ?, ?, ?)"
        );

        statement.setLong(1, transactionLog.timestamp());
        statement.setString(2, transactionLog.transactionType().name());
        statement.setString(3, transactionLog.transactionAmount().toString());
        statement.setString(4, transactionLog.sourceUUID().toString());
        statement.setString(5, transactionLog.offsetUUID().toString());
        statement.setString(6, transactionLog.message());
        statement.executeUpdate();
    }

    public static void insertAuditLog(Connection connection, AuditLog auditLog) throws Exception {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bankconomy_audits (`timestamp`, audit_type, related_uuid, message) VALUES (?, ?, ?, ?)"
        );

        statement.setLong(1, auditLog.timestamp());
        statement.setString(2, auditLog.auditType().name());
        statement.setString(3, auditLog.relatedUUID().toString());
        statement.setString(4, auditLog.message());
        statement.executeUpdate();
    }

    public static void updatePlayerBalance(Connection connection, PlayerAccount playerAccount) throws Exception {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE bankconomy_players SET player_balance = ? WHERE player_uuid = ?"
        );

        statement.setString(1, playerAccount.playerBalance().toString());
        statement.setString(2, playerAccount.playerUUID().toString());
        statement.executeUpdate();
    }

    public static void updateSavingsBalance(Connection connection, SavingsAccount savingsAccount) throws Exception {
        PreparedStatement statement = connection.prepareStatement(
                "UPDATE bankconomy_savings SET savings_balance = ? WHERE savings_uuid = ?"
        );

        statement.setString(1, savingsAccount.savingsBalance().toString());
        statement.setString(2, savingsAccount.savingsUUID().toString());
        statement.executeUpdate();
    }
}
