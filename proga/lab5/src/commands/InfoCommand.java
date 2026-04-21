package commands;

import collection.CollectionManager;


public class InfoCommand implements Command {
    private CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.info());
    }

    public String getDescription() {
        return "вывести информацию о коллекции";
    }
}