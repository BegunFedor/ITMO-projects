package server;

import common.network.CommandRequest;
import common.network.CommandResponse;
import server.commands.*;
import server.connection.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class ServerApp {
    public static final int PORT = 12345;
    public static final String DEFAULT_FILE = "data1.json";

    public static void main(String[] args) {
        System.out.println("[SERVER] Запуск сервера на порту " + PORT);

        FileManager fileManager = new FileManager(DEFAULT_FILE);
        CollectionManager collectionManager = new CollectionManager(fileManager);
        collectionManager.load(DEFAULT_FILE);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[SERVER] Завершение работы. Сохраняем коллекцию...");
            collectionManager.save(DEFAULT_FILE);
        }));

        CommandExecutor commandExecutor = new CommandExecutor();
        registerCommands(commandExecutor, collectionManager, fileManager);

        SaveCommand saveCommand = new SaveCommand(collectionManager, fileManager);
        startAdminConsole(saveCommand);
        
        RequestHandler requestHandler = new RequestHandler(commandExecutor);
        ServerConnection serverConnection = new ServerConnection(PORT);
        ResponseSender responseSender = new ResponseSender();

        while (true) {
            try {
                SocketChannel clientChannel = serverConnection.acceptConnection();
                SocketAddress remoteAddress = clientChannel.getRemoteAddress();
                System.out.println("[SERVER] Клиент подключен: " + remoteAddress);

                while (clientChannel.isConnected()) {
                    CommandRequest request = RequestReader.read(clientChannel);
                    if (request == null) {
                        System.out.println("[SERVER] Получен пустой запрос или соединение закрыто.");
                        break;
                    }

                    //  Блокировка 'save' с клиента
                    if (request.getCommandName().equalsIgnoreCase("save")) {
                        CommandResponse denial = new CommandResponse("Команда 'save' доступна только серверу.", false);
                        responseSender.send(denial, clientChannel);
                        continue;
                    }

                    System.out.println("[SERVER] Получена команда: " + request.getCommandName());
                    CommandResponse response = requestHandler.handle(request);
                    responseSender.send(response, clientChannel);

                    if (request.getCommandName().equalsIgnoreCase("exit")) {
                        System.out.println("[SERVER] Клиент завершил сессию.");
                        break;
                    }
                }

                clientChannel.close();
                System.out.println("[SERVER] Соединение с клиентом закрыто.");

            } catch (IOException e) {
                System.err.println("[SERVER] Ошибка соединения: " + e.getMessage());
            }
        }
    }
    private static void registerCommands(CommandExecutor executor, CollectionManager collectionManager, FileManager fileManager) {
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

    }

    private static void startAdminConsole(SaveCommand saveCommand) {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("[SERVER ADMIN] > ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("save")) {
                    saveCommand.execute(new String[0], null);
                } else {
                    System.out.println("[SERVER ADMIN] Неизвестная команда. Доступно: save");
                }
            }
        }).start();
    }
}