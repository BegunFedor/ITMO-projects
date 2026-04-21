package commands;

import collection.CollectionManager;

public class RemoveByIdCommand implements CommandWithArguments {
    private CollectionManager collectionManager;
    private String[] commandArguments;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        try {
            int id = Integer.parseInt(commandArguments[0]);
            if (collectionManager.removeById(id)) {
                System.out.println("Элемент удален.");
            } else {
                System.out.println("Элемент с таким ID не найден.");
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Ошибка: некорректный ID.");
        }
    }

    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    public String getDescription() {
        return "удалить элемент по ID";
    }
}
