package server;

import common.network.CommandRequest;
import common.network.CommandResponse;
import server.commands.*;
import server.connection.*;
import server.database.DatabaseManager;
import server.database.PostgresStudyGroupDao;
import server.database.PostgresUserDao;
import server.database.StudyGroupDao;
import server.database.UserDao;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.*;

public class ServerApp {
    public static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("[SERVER] Запуск сервера на порту " + PORT);

        Connection dbConnection = DatabaseManager.getConnection();
        if (dbConnection == null) {
            System.err.println("[SERVER] Не удалось подключиться к БД. Завершение.");
            return;
        }

        UserDao userDao = new PostgresUserDao(dbConnection);
        StudyGroupDao studyGroupDao = new PostgresStudyGroupDao(dbConnection);
        CollectionManager collectionManager = new CollectionManager(studyGroupDao);

        Set<String> activeUsers = ConcurrentHashMap.newKeySet();

        CommandExecutor executor = new CommandExecutor();
        registerCommands(executor, collectionManager, userDao, activeUsers);

        RequestHandler handler = new RequestHandler(executor, userDao);
        ServerConnection serverConnection = new ServerConnection(PORT);
        ResponseSender responseSender = new ResponseSender();

        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        while (true) {
            try {
                SocketChannel clientChannel = serverConnection.acceptConnection();
                SocketAddress remoteAddress = clientChannel.getRemoteAddress();
                System.out.println("[SERVER] Клиент подключен: " + remoteAddress);

                new Thread(() -> {
                    String currentUser = null;

                    try {
                        while (clientChannel.isConnected()) {
                            CommandRequest request = RequestReader.read(clientChannel);
                            if (request == null) break;

                            if (request.getCommandName().equalsIgnoreCase("login")) {
                                currentUser = request.getUsername();
                            }

                            CommandRequest finalRequest = request;
                            threadPool.submit(() -> {
                                CommandResponse response = handler.handle(finalRequest);
                                responseSender.send(response, clientChannel);
                            });

                            if (request.getCommandName().equalsIgnoreCase("exit")) break;
                        }
                    } catch (Exception e) {
                        System.err.println("[SERVER] Ошибка при обработке запроса: " + e.getMessage());
                    } finally {
                        try {
                            clientChannel.close();
                        } catch (IOException ignored) {}

                        if (currentUser != null) {
                            activeUsers.remove(currentUser);
                            System.out.println("[SERVER] Пользователь " + currentUser + " вышел из системы.");
                        }

                        System.out.println("[SERVER] Соединение закрыто.");
                    }
                }).start();

            } catch (IOException e) {
                System.err.println("[SERVER] Ошибка подключения клиента: " + e.getMessage());
            }
        }
    }

    private static void registerCommands(CommandExecutor executor,
                                         CollectionManager collectionManager,
                                         UserDao userDao,
                                         Set<String> activeUsers) {
        executor.register(new HelpCommand(executor));
        executor.register(new InfoCommand(collectionManager));
        executor.register(new ShowCommand(collectionManager));
        executor.register(new AddCommand(collectionManager));
        executor.register(new UpdateCommand(collectionManager));
        executor.register(new RemoveByIdCommand(collectionManager));
        executor.register(new ClearCommand(collectionManager));
        executor.register(new AddIfMaxCommand(collectionManager));
        executor.register(new AddIfMinCommand(collectionManager));
        executor.register(new RemoveGreaterCommand(collectionManager));
        executor.register(new RemoveAllByGroupAdminCommand(collectionManager));
        executor.register(new FilterGreaterThanGroupAdminCommand(collectionManager));
        executor.register(new PrintFieldDescendingSemesterEnumCommand(collectionManager));
        executor.register(new ExitCommand());
        executor.register(new ExecuteScriptCommand());
        executor.register(new RegisterCommand(userDao));
        executor.register(new LoginCommand(userDao, activeUsers));
    }
}