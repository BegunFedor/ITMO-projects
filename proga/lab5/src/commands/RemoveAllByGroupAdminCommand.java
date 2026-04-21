package commands;

import collection.CollectionManager;
import collection.Person;


public class RemoveAllByGroupAdminCommand implements CommandWithArguments {
    private CollectionManager collectionManager;
    private String[] commandArguments;

    public RemoveAllByGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        if (commandArguments.length == 0) {
            System.err.println("Ошибка: не указан groupAdmin.");
            return;
        }

        String adminName = commandArguments[0];
        collectionManager.getCollection().values()
                .removeIf(group -> group.getGroupAdmin().getName().equalsIgnoreCase(adminName));

        System.out.println("Удалены все элементы с groupAdmin = " + adminName);
    }

    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    public String getDescription() {
        return "удалить все элементы с указанным groupAdmin";
    }
}
