package server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostgresUserDao implements UserDao {
    private final Connection connection;

    public PostgresUserDao(Connection connection) {
        this.connection = connection;
        try {
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("[DB] Не удалось включить автокоммит: " + e.getMessage());
        }
    }

    @Override
    public boolean createUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("[DB] Пользователь с таким именем уже существует.");
            } else {
                System.err.println("[DB] Ошибка при создании пользователя: " + e.getMessage());
            }
            return false;
        }
    }

    @Override
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при проверке пользователя: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean authenticate(String username, String passwordHash) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка авторизации пользователя: " + e.getMessage());
            return false;
        }
    }
}