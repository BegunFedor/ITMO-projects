package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class RemoveGreaterCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveGreaterCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "remove_greater";
    }

    @Override
    public String getDescription() {
        return "удалить все элементы, превышающие заданный, если они принадлежат пользователю";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (!(payload instanceof StudyGroup)) {
            return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);
        }

        int removed = collectionManager.removeGreater((StudyGroup) payload, username);
        return new CommandResponse("Удалено " + removed + " элементов.", true);
    }
}