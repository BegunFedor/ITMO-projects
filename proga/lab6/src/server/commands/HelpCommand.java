package server.commands;

import common.network.CommandResponse;
import server.connection.CommandExecutor;

public class HelpCommand implements ServerCommand {
    private final CommandExecutor executor;

    public HelpCommand(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "вывести справку по доступным командам";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        StringBuilder help = new StringBuilder("Список доступных команд:\n");
        executor.getCommands().forEach((name, cmd) -> help.append(name).append(" - ").append(cmd.getDescription()).append("\n"));
        return new CommandResponse(help.toString(), true);
    }
}