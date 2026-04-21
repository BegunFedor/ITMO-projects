package client.network;

import client.StudyGroupFieldsReader;
import common.network.CommandRequest;
import server.auth.PasswordHasher;

import java.util.Scanner;

public class CommandReader {
    private final Scanner scanner;
    private final StudyGroupFieldsReader fieldsReader;

    public CommandReader(Scanner scanner, StudyGroupFieldsReader fieldsReader) {
        this.scanner = scanner;
        this.fieldsReader = fieldsReader;
    }

    public CommandRequest readAuthCommand() {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split(" ");
        if (parts.length != 3) {
            System.out.println("Неверный формат. Используйте: login <login> <password> или register <login> <password>");
            return null;
        }

        String command = parts[0];
        String login = parts[1];
        String password = parts[2];
        String hash = PasswordHasher.hash(password);

        if (!command.equalsIgnoreCase("login") && !command.equalsIgnoreCase("register")) {
            System.out.println("Неизвестная команда.");
            return null;
        }

        return new CommandRequest(command, new String[]{login, password}, null, login, hash);
    }

    public CommandRequest readCommand(String username, String passwordHash) {
        System.out.print("> ");
        if (!scanner.hasNextLine()) return null;

        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split(" ", 2);
        String command = parts[0];
        String[] args = (parts.length > 1) ? parts[1].split(" ") : new String[]{};

        Object payload = null;

        switch (command) {
            case "add":
            case "add_if_max":
            case "add_if_min":
            case "remove_greater":
            case "update":
                payload = fieldsReader.readStudyGroup();
                break;
            case "remove_all_by_group_admin":
            case "filter_greater_than_group_admin":
                payload = fieldsReader.readPerson();
                break;
        }

        return new CommandRequest(command, args, payload, username, passwordHash);
    }
}