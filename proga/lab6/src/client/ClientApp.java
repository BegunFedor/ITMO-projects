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
            System.out.println("[КЛИЕНТ] Сервер недоступен. Завершение работы.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        StudyGroupFieldsReader fieldsReader = new StudyGroupFieldsReader(scanner);
        CommandReader commandReader = new CommandReader(scanner, fieldsReader);

        while (true) {
            try {
                CommandRequest request = commandReader.readCommand();
                if (request == null) continue;

                connection.sendRequest(request);
                CommandResponse response = connection.receiveResponse();

                if (response != null) {
                    System.out.println(response.getMessage());
                    if (request.getCommandName().equalsIgnoreCase("exit")) {
                        System.out.println("[КЛИЕНТ] Завершение работы клиента.");
                        break;
                    }
                } else {
                    System.out.println("[КЛИЕНТ] Ответ от сервера не получен.");
                }

            } catch (Exception e) {
                System.err.println("[КЛИЕНТ] Ошибка при работе с сервером: " + e.getMessage());
                break;
            }
        }

        connection.close();
    }
}