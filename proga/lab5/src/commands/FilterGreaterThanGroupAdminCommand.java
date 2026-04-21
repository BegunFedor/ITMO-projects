package commands;

import collection.CollectionManager;
import collection.StudyGroup;

import java.util.List;
import java.util.stream.Collectors;

public class FilterGreaterThanGroupAdminCommand implements CommandWithArguments {
    private CollectionManager collectionManager;
    private String[] commandArguments;

    public FilterGreaterThanGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        if (commandArguments.length == 0) {
            System.err.println("Ошибка: не указан groupAdmin.");
            return;
        }

        String adminName = commandArguments[0];

        List<StudyGroup> filtered = collectionManager.getCollection().values().stream()
                .filter(group -> group.getGroupAdmin().getName().compareToIgnoreCase(adminName) > 0)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            System.out.println("Нет элементов, у которых groupAdmin больше указанного.");
        } else {
            System.out.println(collectionManager.formatGroups(filtered));
        }
    }

    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    public String getDescription() {
        return "вывести элементы, у которых groupAdmin больше заданного";
    }
}