package server.database;

public interface UserDao {
    boolean createUser(String username, String passwordHash);
    boolean userExists(String username);
    boolean authenticate(String username, String passwordHash);
}