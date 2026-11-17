package com.minibank.factory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {
    private final static DataSource DATA_SOURCE;
    private final static String BASE_URL = "jdbc:postgresql://localhost:5432/mini_bank";
    private final static String USER = "user";
    private final static String PASSWORD = "pass";

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(BASE_URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(300_000);

        DATA_SOURCE = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
