package commands;

import collection.CollectionManager;

public class ClearCommand implements Command {
    private CollectionManager collectionManager;

    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        collectionManager.clear();
        System.out.println("Коллекция очищена.");
    }

    public String getDescription() {
        return "очистить коллекцию";
    }
}