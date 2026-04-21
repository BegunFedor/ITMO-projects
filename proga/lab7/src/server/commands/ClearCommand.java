package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

public class ClearCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public ClearCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getDescription() {
        return "очистить коллекцию";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        collectionManager.clear(username); // только свои
        return new CommandResponse("Все ваши элементы удалены из коллекции.", true);
    }
}