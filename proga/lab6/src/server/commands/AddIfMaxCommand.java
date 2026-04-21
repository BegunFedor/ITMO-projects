package server.commands;

import server.CollectionManager;
import common.models.StudyGroup;
import common.network.CommandResponse;

public class AddIfMaxCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public AddIfMaxCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "add_if_max";
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент, если его значение больше максимального";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (!(payload instanceof StudyGroup)) return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);

        StudyGroup group = (StudyGroup) payload;
        if (collectionManager.isMax(group)) {
            collectionManager.add(group);
            return new CommandResponse("Группа успешно добавлена как максимальная.", true);
        } else {
            return new CommandResponse("Группа не максимальна. Не добавлена.", false);
        }
    }
}