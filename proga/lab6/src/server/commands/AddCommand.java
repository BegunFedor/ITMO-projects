package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class AddCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public AddCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (!(payload instanceof StudyGroup)) {
            return new CommandResponse("Ошибка: Ожидался объект StudyGroup.", false);
        }

        StudyGroup group = (StudyGroup) payload;
        collectionManager.add(group);
        return new CommandResponse("Группа успешно добавлена.", true);
    }
}