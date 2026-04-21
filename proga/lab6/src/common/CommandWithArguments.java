package common;

public interface CommandWithArguments extends Command {
    void getCommandArguments(String[] commandArguments);
}
