package com.minibank.service;

import com.minibank.entity.Client;
import com.minibank.factory.ConnectionFactory;

import java.sql.SQLException;

public class ClientService {
    public Client registerClient(String name, String email) {
        String sql = "INSERT INTO clients (name, email) VALUES (?, ?) RETURNING id";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ps.setString(2, email);

            try (var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return new Client(resultSet.getLong("id"), name, email);
                }
                throw new RuntimeException("Failed to create a user");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Client getClient(Long clientId) {
        String sql = "SELECT id, name, email FROM clients WHERE id = ?";

        try (
                var connection = ConnectionFactory.getConnection();
                var ps = connection.prepareStatement(sql)
        ) {
            ps.setLong(1, clientId);

            try (var resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return new Client(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            resultSet.getString("email")
                    );
                }
                throw new IllegalArgumentException("Client not found: " + clientId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
