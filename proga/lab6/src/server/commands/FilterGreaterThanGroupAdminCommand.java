package server.commands;

import server.CollectionManager;
import common.models.Person;
import common.network.CommandResponse;

public class FilterGreaterThanGroupAdminCommand implements ServerCommand {
    private final CollectionManager collectionManager;

    public FilterGreaterThanGroupAdminCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public String getName() {
        return "filter_greater_than_group_admin";
    }

    @Override
    public String getDescription() {
        return "вывести элементы, значение поля groupAdmin которых больше заданного";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        if (!(payload instanceof Person)) return new CommandResponse("Ошибка: ожидался объект Person.", false);

        String result = collectionManager.filterGreaterThanGroupAdmin((Person) payload);
        return new CommandResponse(result, true);
    }
}