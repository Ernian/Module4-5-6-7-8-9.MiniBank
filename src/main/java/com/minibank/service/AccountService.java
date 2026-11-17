package com.minibank.service;

import com.minibank.entity.Account;
import com.minibank.factory.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

public class AccountService {

    public Account openAccount(Long clientId, String accountNumber, BigDecimal balance) {
        String sql = "INSERT INTO accounts (client_id, account_number, balance) VALUES (?, ?, ?) RETURNING id";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, clientId);
            ps.setString(2, accountNumber);
            ps.setBigDecimal(3, balance);

            try (var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return new Account(
                            resultSet.getLong("id"),
                            clientId,
                            accountNumber,
                            balance
                    );
                }
                throw new RuntimeException("Failed to create an account");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Account openAccount(Long clientId, String accountNumber) {
        return openAccount(clientId, accountNumber, new BigDecimal(0));
    }

    public Account getAccount(Long accountId) {
        String sql = "SELECT id, client_id, account_number, balance FROM accounts WHERE id = ?";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, accountId);

            try (var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return new Account(
                            accountId,
                            resultSet.getLong("client_id"),
                            resultSet.getString("account_number"),
                            resultSet.getBigDecimal("balance")
                    );
                }
                throw new IllegalArgumentException("Account not found: " + accountId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void updateBalance(Long accountId, BigDecimal balance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setBigDecimal(1, balance);
            ps.setLong(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
