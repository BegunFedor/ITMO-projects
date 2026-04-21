package common;

public interface Command {
    void execute();

    default String getDescription() {
        return "Описание работы данной команды еще не реализовано";
    }
}