package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

import java.util.ArrayList;

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
        return new CommandResponse("ОК", true, new ArrayList<>(collectionManager.getCollection().values()));
    }
}