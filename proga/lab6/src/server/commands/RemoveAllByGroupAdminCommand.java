package server.commands;

import server.CollectionManager;
import common.models.Person;
import common.network.CommandResponse;

public class RemoveAllByGroupAdminCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public RemoveAllByGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "remove_all_by_group_admin";
    }

    @Override
    public String getDescription() {
        return "удалить из коллекции все элементы, значение поля groupAdmin которых эквивалентно заданному";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (!(payload instanceof Person)) return new CommandResponse("Ошибка: ожидался объект Person.", false);

        int removed = collectionManager.removeAllByGroupAdmin((Person) payload);
        return new CommandResponse("Удалено " + removed + " элементов.", true);
    }
}