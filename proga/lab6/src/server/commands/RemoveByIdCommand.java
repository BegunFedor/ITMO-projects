package server.commands;

import server.CollectionManager;
import common.network.CommandResponse;

public class RemoveByIdCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "remove_by_id";
    }

    @Override
    public String getDescription() {
        return "удалить элемент по ID";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (args.length < 1) {
            return new CommandResponse("Ошибка: необходимо указать ID.", false);
        }

        try {
            int id = Integer.parseInt(args[0]);
            boolean removed = collectionManager.removeById(id);
            return new CommandResponse(removed ? "Элемент удалён." : "Элемент с таким ID не найден.", removed);
        } catch (NumberFormatException e) {
            return new CommandResponse("Ошибка: ID должен быть целым числом.", false);
        }
    }
}