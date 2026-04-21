package server.commands;

import common.network.CommandResponse;

public class ExecuteScriptCommand implements ServerCommand {

    @Override
    public String getName() {
        return "execute_script";
    }

    @Override
    public String getDescription() {
        return "выполнить скрипт из файла (команда недоступна серверу)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        return new CommandResponse("Команда execute_script запрещена на сервере.", false);
    }
}