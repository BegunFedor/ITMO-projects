package server.commands;

import common.network.CommandResponse;

public class ExitCommand implements ServerCommand {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "завершить выполнение программы (клиент)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        return new CommandResponse("Завершение клиента.", true);
    }
}