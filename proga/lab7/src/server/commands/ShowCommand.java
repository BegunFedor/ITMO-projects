package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

public class ShowCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public String getDescription() {
        return "показать все элементы коллекции";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        return new CommandResponse(collectionManager.showFormatted(), true);
    }
}