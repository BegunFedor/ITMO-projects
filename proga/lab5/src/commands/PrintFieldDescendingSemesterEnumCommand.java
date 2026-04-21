package commands;

import collection.CollectionManager;


public class PrintFieldDescendingSemesterEnumCommand implements Command {
    private CollectionManager collectionManager;

    public PrintFieldDescendingSemesterEnumCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public void execute() {
        System.out.println(collectionManager.printFieldDescendingSemesterEnum());
    }

    public String getDescription() {
        return "вывести semesterEnum в порядке убывания";
    }
}