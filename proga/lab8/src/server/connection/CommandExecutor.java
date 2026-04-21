package server.connection;

import common.network.CommandRequest;
import common.network.CommandResponse;
import server.commands.ServerCommand;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private final Map<String, ServerCommand> commands = new HashMap<>();

    public void register(ServerCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    public Map<String, ServerCommand> getCommands() {
        return commands;
    }

    public CommandResponse execute(CommandRequest request) {
        ServerCommand command = commands.get(request.getCommandName().toLowerCase());

        if (command == null) {
            return new CommandResponse("Команда не найдена: " + request.getCommandName(), false);
        }

        try {
            return command.execute(request.getArguments(), request.getPayload(), request.getUsername());
        } catch (Exception e) {
            return new CommandResponse("Ошибка при выполнении команды: " + e.getMessage(), false);
        }
    }
}