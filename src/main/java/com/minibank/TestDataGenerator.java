package com.minibank;

import com.minibank.factory.ConnectionFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TestDataGenerator {
    private static final int CLIENT_COUNT = 10;
    private static final int ACCOUNTS_PER_CLIENT = 2;
    private static final int TRANSFER_COUNT = 25_000;
    private static final int BATCH_SIZE = 1_000;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);

            long start = System.currentTimeMillis();

            cleanAll(connection);
            List<Long> clientIds = insertClients(connection);
            List<Long> accountIds = insertAccounts(connection, clientIds);
            int inserted = insertTransactions(connection, accountIds);

            connection.commit();

            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("Готово за %d мс%n", elapsed);
            System.out.printf("Клиентов:     %d%n", clientIds.size());
            System.out.printf("Аккаунтов:    %d%n", accountIds.size());
            System.out.printf("Транзакций:   %d%n", inserted);

            verify(connection);
        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    // ==================================================================
    // 1. Очистка (аналог TRUNCATE ... RESTART IDENTITY CASCADE)
    // ==================================================================
    private static void cleanAll(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("TRUNCATE transactions RESTART IDENTITY CASCADE");
            st.executeUpdate("TRUNCATE accounts    RESTART IDENTITY CASCADE");
            st.executeUpdate("TRUNCATE clients     RESTART IDENTITY CASCADE");
        }
        System.out.println("Таблицы очищены");
    }

    // ==================================================================
    // 2. Клиенты: 10 штук
    // ==================================================================
    private static List<Long> insertClients(Connection connection) throws SQLException {
        String sql = "INSERT INTO clients (name, email) VALUES (?, ?) RETURNING id";

        List<Long> clientIds = new ArrayList<>(CLIENT_COUNT);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i <= CLIENT_COUNT; i++) {
                ps.setString(1, "Client " + i);
                ps.setString(2, "client" + i + "@example.com");

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("INSERT клиента не вернул id");
                    }
                    clientIds.add(rs.getLong("id"));
                }
            }
        }

        System.out.printf("Вставлено клиентов: %d%n", clientIds.size());
        return clientIds;
    }

    // ==================================================================
    // 3. Аккаунты: по 2 на клиента, сгенерированный номер через LPAD
    // ==================================================================
    private static List<Long> insertAccounts(Connection conn, List<Long> clientIds)
            throws SQLException {

        String sql = "INSERT INTO accounts (client_id, account_number, balance) " +
                "VALUES (?, ?, ?) RETURNING id";

        List<Long> accountIds = new ArrayList<>(clientIds.size() * ACCOUNTS_PER_CLIENT);
        long accountCounter = 1;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long clientId : clientIds) {
                for (int i = 1; i <= ACCOUNTS_PER_CLIENT; i++) {
                    // LPAD((client_id * 100 + i)::text, 34, '0')
                    String accountNumber = generateAccountNumber(Long.toHexString(clientId + accountCounter++));
                    // Начальный баланс: 1000..10000
                    BigDecimal balance = BigDecimal
                            .valueOf(1000 + RANDOM.nextDouble() * 9000)
                            .setScale(4, RoundingMode.HALF_UP);

                    ps.setLong(1, clientId);
                    ps.setString(2, accountNumber);
                    ps.setBigDecimal(3, balance);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("INSERT аккаунта не вернул id");
                        }
                        accountIds.add(rs.getLong("id"));
                    }
                }
            }
        }

        System.out.printf("Вставлено аккаунтов: %d%n", accountIds.size());
        return accountIds;
    }

    // ==================================================================
    // 4. Транзакции: 25 000 пар × 2 строки = 50 000, батчами
    // ==================================================================
    private static int insertTransactions(Connection conn, List<Long> accountIds)
            throws SQLException {

        String sql = "INSERT INTO transactions (account_id, amount, type, transaction_date) " +
                "VALUES (?, ?, ?, ?)";

        int n = accountIds.size();
        int inserted = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= TRANSFER_COUNT; i++) {
                int fromIndex = RANDOM.nextInt(accountIds.size());
                int toIndex;
                do {
                    toIndex = RANDOM.nextInt(accountIds.size());
                } while (toIndex == fromIndex);

                Long fromAccountId = accountIds.get(fromIndex);
                Long toAccountId = accountIds.get(toIndex);

                BigDecimal amount = generateRandomAmount();
                Timestamp transactionDate = Timestamp.valueOf(generateRandomDate());

                // --- TRANSFER_OUT (-amount) ---
                ps.setLong(1, fromAccountId);
                ps.setBigDecimal(2, amount.negate());
                ps.setString(3, "TRANSFER_OUT");
                ps.setTimestamp(4, transactionDate);
                ps.addBatch();

                // --- TRANSFER_IN (+amount) ---
                ps.setLong(1, toAccountId);
                ps.setBigDecimal(2, amount);
                ps.setString(3, "TRANSFER_IN");
                ps.setTimestamp(4, transactionDate);
                ps.addBatch();

                // Отправляем пачку каждые BATCH_SIZE транзакций (500 переводов)
                if (i % (BATCH_SIZE / 2) == 0) {
                    int[] results = ps.executeBatch();
                    inserted += countSuccessful(results);
                    ps.clearBatch();

                    if (i % 5_000 == 0) {
                        System.out.printf("  ...обработано переводов: %d%n", i);
                    }
                }
            }

            // Остаток
            int[] results = ps.executeBatch();
            inserted += countSuccessful(results);
            ps.clearBatch();
        }

        System.out.printf("Вставлено транзакций: %d%n", inserted);
        return inserted;
    }

    private static BigDecimal generateRandomAmount() {
        double amount = 10 + (RANDOM.nextDouble() * 9990);
        return BigDecimal.valueOf(amount)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static LocalDateTime generateRandomDate() {
        LocalDateTime now = LocalDateTime.now();
        long daysBack = RANDOM.nextInt(365);
        long hoursBack = RANDOM.nextInt(24);
        long minutesBack = RANDOM.nextInt(60);

        return now.minusDays(daysBack)
                .minusHours(hoursBack)
                .minusMinutes(minutesBack);
    }

    // ==================================================================
    // 5. Проверка результата
    // ==================================================================
    private static void verify(Connection conn) throws SQLException {
        System.out.println("\n=== Проверка ===");

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT " +
                             "(SELECT count(*) FROM clients) AS c, " +
                             "(SELECT count(*) FROM accounts) AS a, " +
                             "(SELECT count(*) FROM transactions) AS t, " +
                             "(SELECT coalesce(sum(amount), 0) FROM transactions) AS s")) {

            if (rs.next()) {
                System.out.printf("clients:       %d%n", rs.getLong("c"));
                System.out.printf("accounts:      %d%n", rs.getLong("a"));
                System.out.printf("transactions:  %d%n", rs.getLong("t"));
                System.out.printf("sum(amount):   %s%n", rs.getBigDecimal("s"));
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT type, count(*) AS cnt FROM transactions " +
                             "GROUP BY type ORDER BY type")) {
            System.out.println("\nПо типам:");
            while (rs.next()) {
                System.out.printf("  %-14s %d%n",
                        rs.getString("type"), rs.getLong("cnt"));
            }
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT client_id, count(*) FROM accounts " +
                             "GROUP BY client_id ORDER BY client_id")) {
            System.out.println("\nАккаунтов на клиента:");
            while (rs.next()) {
                System.out.printf("  client_id=%d: %d%n",
                        rs.getLong(1), rs.getLong(2));
            }
        }
    }

    private static String generateAccountNumber(String s) {
        if (s.length() >= 34) {
            return s;
        }
        StringBuilder sb = new StringBuilder("ACC");
        for (int i = 0; i < 31 - s.length(); i++) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Считает успешные вставки в результате executeBatch().
     * По контракту JDBC: 0 или положительное число — успех,
     * Statement.SUCCESS_NO_INFO (-2) — успех без информации о количестве,
     * Statement.EXECUTE_FAILED (-3) — ошибка.
     */
    private static int countSuccessful(int[] results) {
        int ok = 0;
        for (int r : results) {
            if (r >= 0 || r == Statement.SUCCESS_NO_INFO) {
                ok++;
            }
        }
        return ok;
    }
}