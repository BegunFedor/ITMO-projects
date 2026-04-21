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
        return "добавить новый элемент в коллекцию (только авторизованный пользователь может добавить объект)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        if (username == null || username.isEmpty()) {
            return new CommandResponse("Ошибка: Вы не авторизованы.", false);
        }

        if (!(payload instanceof StudyGroup)) {
            return new CommandResponse("Ошибка: ожидался объект StudyGroup.", false);
        }

        StudyGroup group = (StudyGroup) payload;
        group.setOwner(username); // устанавливаем владельца
        collectionManager.add(group, username);
        return new CommandResponse("Группа успешно добавлена.", true);
    }
}