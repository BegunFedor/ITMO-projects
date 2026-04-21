package server.commands;

import common.network.CommandResponse;

public class ExitCommand implements ServerCommand {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "завершить выполнение программы (только клиент)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        return new CommandResponse("Клиент завершает работу.", true);
    }
}