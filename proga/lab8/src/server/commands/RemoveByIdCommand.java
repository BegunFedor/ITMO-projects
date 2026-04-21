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
        return "удалить элемент по ID (если принадлежит пользователю)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (args.length < 1) {
            return new CommandResponse("Ошибка: необходимо указать ID.", false);
        }

        try {
            int id = Integer.parseInt(args[0]);
            boolean removed = collectionManager.removeById(id, username);
            return new CommandResponse(removed ? "Элемент удалён." : "Элемент не найден или не принадлежит вам.", removed);
        } catch (NumberFormatException e) {
            return new CommandResponse("Ошибка: ID должен быть целым числом.", false);
        }
    }
}