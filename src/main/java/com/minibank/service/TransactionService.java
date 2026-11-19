package com.minibank.service;

import com.minibank.factory.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.SQLException;

public class TransactionService {
    public boolean recordTransaction(Long accountId, BigDecimal amount, String type) {
        String sql = "INSERT INTO transactions (account_id, amount, type) VALUES (?, ?, ?)";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, accountId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, type);

            int rowsAffected  = ps.executeUpdate();
            return rowsAffected  == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
