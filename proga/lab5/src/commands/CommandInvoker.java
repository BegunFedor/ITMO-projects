package commands;

import java.util.HashMap;
import java.util.Map;

public class CommandInvoker {
    private Map<String, Command> commands = new HashMap<>();

    public void register(String name, Command command) {
        commands.put(name, command);
    }


    public void execute(String name, String[] args) {
        if (commands.containsKey(name)) {
            Command command = commands.get(name);
            if (command instanceof CommandWithArguments) {
                ((CommandWithArguments) command).getCommandArguments(args);
            }
            command.execute();
        } else {
            System.out.println("Команда не найдена!");
        }
    }

    public void showAvailableCommands() {
        System.out.println("Доступные команды:");
        commands.forEach((name, command) -> System.out.println(name + " - " + command.getDescription()));
    }
}
