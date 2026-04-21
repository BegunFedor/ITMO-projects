package client;

import client.network.ClientConnection;
import client.network.CommandReader;
import common.network.CommandRequest;
import common.network.CommandResponse;


import java.util.Scanner;

public class ClientApp {
    private static final int PORT = 12345;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        System.out.println("[КЛИЕНТ] Запуск клиента и подключение к серверу...");

        ClientConnection connection = new ClientConnection(HOST, PORT);
        if (!connection.connect()) {
            System.out.println("Сервер недоступен.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        StudyGroupFieldsReader fieldsReader = new StudyGroupFieldsReader(scanner);
        CommandReader commandReader = new CommandReader(scanner, fieldsReader);

        String username = null;
        String passwordHash = null;


        while (true) {
            System.out.print("Введите команду (login <login> <password> или register <login> <password>): ");
            CommandRequest authRequest = commandReader.readAuthCommand();
            if (authRequest == null) continue;

            try {
                connection.sendRequest(authRequest);
                CommandResponse response = connection.receiveResponse();
                System.out.println(response.getMessage());

                if (response.isSuccess()) {
                    username = authRequest.getUsername();
                    passwordHash = authRequest.getPasswordHash();
                    break;
                }
            } catch (Exception e) {
                System.err.println("Ошибка при авторизации: " + e.getMessage());
            }
        }

        while (true) {
            try {
                CommandRequest request = commandReader.readCommand(username, passwordHash);
                if (request == null) continue;

                request = new CommandRequest(
                        request.getCommandName(),
                        request.getArguments(),
                        request.getPayload(),
                        username,
                        passwordHash
                );

                connection.sendRequest(request);
                CommandResponse response = connection.receiveResponse();

                if (response != null) {
                    System.out.println(response.getMessage());
                    if (request.getCommandName().equalsIgnoreCase("exit")) break;
                } else {
                    System.out.println("Нет ответа от сервера.");
                }

            } catch (Exception e) {
                System.err.println("Ошибка при обмене с сервером: " + e.getMessage());
                break;
            }
        }

        connection.close();
    }
}