package commands;

public class ExitCommand implements Command {
    @Override
    public void execute() {
        System.out.println("Выход из программы.");
        System.exit(0);
    }

    public String getDescription() {
        return "завершить программу";
    }
}