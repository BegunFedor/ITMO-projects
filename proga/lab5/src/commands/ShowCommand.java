package commands;

import collection.CollectionManager;

public class ShowCommand implements Command {
    private CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        String output = collectionManager.showFormatted();
        System.out.println(output);
    }

    public String getDescription() {
        return "вывести все элементы коллекции";
    }
}
