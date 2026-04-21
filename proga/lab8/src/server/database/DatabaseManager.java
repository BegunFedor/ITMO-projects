package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5433/studs"; //
    private static final String USER = "s471733"; //
    private static final String PASSWORD = "hFBnMAl1UyrfGp05"; //

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Не удалось загрузить драйвер PostgreSQL");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);
            System.out.println("[DB] Подключение к базе данных установлено");
            return connection;
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка подключения к базе данных: " + e.getMessage());
            return null;
        }
    }
}