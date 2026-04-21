

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5433/studs";
        String user = "s471733"; // замени на свой логин
        String password = "hFBnMAl1UyrfGp05"; // и пароль

        try {
            Class.forName("org.postgresql.Driver"); // загрузка драйвера
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Подключение установлено!");
            connection.close();
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер PostgreSQL не найден.");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
        }
    }
}