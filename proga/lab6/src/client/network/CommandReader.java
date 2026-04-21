package client.network;

import common.network.CommandRequest;
import common.models.StudyGroup;
import client.StudyGroupFieldsReader;

import java.util.Scanner;

public class CommandReader {
    private final Scanner scanner;
    private final StudyGroupFieldsReader fieldsReader;

    public CommandReader(Scanner scanner, StudyGroupFieldsReader fieldsReader) {
        this.scanner = scanner;
        this.fieldsReader = fieldsReader;
    }

    public CommandRequest readCommand() {
        System.out.print("> ");
        if (!scanner.hasNextLine()) return new CommandRequest("exit", new String[]{}, null);

        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split(" ", 2);
        String commandName = parts[0];
        String[] arguments = (parts.length > 1) ? new String[]{parts[1]} : new String[]{};

        if (commandName.equals("add") || commandName.equals("add_if_max") ||
                commandName.equals("add_if_min") || commandName.equals("remove_greater") ||
                commandName.equals("update")) {
            StudyGroup group = fieldsReader.readStudyGroup();
            return new CommandRequest(commandName, arguments, group);
        }

        return new CommandRequest(commandName, arguments, null);
    }
}