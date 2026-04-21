package commands;


public class HelpCommand implements Command {
    private CommandInvoker invoker;

    public HelpCommand(CommandInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public void execute() {
        invoker.showAvailableCommands();
    }

    public String getDescription() {
        return "вывести справку по доступным командам";
    }
}