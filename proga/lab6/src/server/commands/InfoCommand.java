package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

public class InfoCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "вывести информацию о коллекции";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        return new CommandResponse(collectionManager.info(), true);
    }
}