package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class UpdateCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public UpdateCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "обновить элемент коллекции по ID (если он принадлежит пользователю)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (args.length < 1) return new CommandResponse("Ошибка: необходимо указать ID.", false);
        if (!(payload instanceof StudyGroup)) return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);

        try {
            int id = Integer.parseInt(args[0]);
            boolean result = collectionManager.update(id, (StudyGroup) payload, username);
            return new CommandResponse(result ? "Элемент обновлён." : "Элемент не найден или не принадлежит вам.", result);
        } catch (NumberFormatException e) {
            return new CommandResponse("Ошибка: ID должен быть числом.", false);
        }
    }
}