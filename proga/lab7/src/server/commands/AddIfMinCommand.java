package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class AddIfMinCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public AddIfMinCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "add_if_min";
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент, если его значение меньше минимального";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (!(payload instanceof StudyGroup)) {
            return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);
        }

        StudyGroup group = (StudyGroup) payload;
        group.setOwner(username);

        if (collectionManager.isMin(group)) {
            collectionManager.add(group, username);
            return new CommandResponse("Группа успешно добавлена как минимальная.", true);
        } else {
            return new CommandResponse("Группа не минимальна. Не добавлена.", false);
        }
    }
}