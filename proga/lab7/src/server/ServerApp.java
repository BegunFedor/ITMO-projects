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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    public static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("[SERVER] Запуск сервера на порту " + PORT);

        // Подключение к БД
        Connection dbConnection = DatabaseManager.getConnection();
        if (dbConnection == null) {
            System.err.println("[SERVER] Не удалось подключиться к БД. Завершение.");
            return;
        }

        // DAO и сервисы
        UserDao userDao = new PostgresUserDao(dbConnection);
        StudyGroupDao studyGroupDao = new PostgresStudyGroupDao(dbConnection); // ✅ добавлено
        CollectionManager collectionManager = new CollectionManager(studyGroupDao); // ✅ исправлено

        CommandExecutor executor = new CommandExecutor();
        registerCommands(executor, collectionManager, userDao);

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
                    while (clientChannel.isConnected()) {
                        try {
                            CommandRequest request = RequestReader.read(clientChannel);
                            if (request == null) break;

                            threadPool.submit(() -> {
                                CommandResponse response = handler.handle(request);
                                responseSender.send(response, clientChannel);
                            });

                            if (request.getCommandName().equalsIgnoreCase("exit")) break;

                        } catch (Exception e) {
                            System.err.println("[SERVER] Ошибка при обработке запроса: " + e.getMessage());
                            break;
                        }
                    }

                    try {
                        clientChannel.close();
                    } catch (IOException ignored) {}
                    System.out.println("[SERVER] Соединение закрыто.");
                }).start();

            } catch (IOException e) {
                System.err.println("[SERVER] Ошибка подключения клиента: " + e.getMessage());
            }
        }
    }

    private static void registerCommands(CommandExecutor executor, CollectionManager collectionManager, UserDao userDao) {
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
        executor.register(new LoginCommand(userDao));
    }
}