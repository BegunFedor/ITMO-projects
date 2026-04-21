package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class
RemoveGreaterCommand implements ServerCommand {
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
        return "удалить все элементы, превышающие заданный";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (!(payload instanceof StudyGroup)) return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);

        collectionManager.removeGreater((StudyGroup) payload);
        return new CommandResponse("Удаление завершено.", true);
    }
}