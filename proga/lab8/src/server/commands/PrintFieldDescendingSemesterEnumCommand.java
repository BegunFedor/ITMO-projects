package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

public class PrintFieldDescendingSemesterEnumCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public PrintFieldDescendingSemesterEnumCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "print_field_descending_semester_enum";
    }

    @Override
    public String getDescription() {
        return "вывести значения поля semesterEnum в порядке убывания";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        return new CommandResponse(collectionManager.printFieldDescendingSemesterEnum(), true);
    }
}