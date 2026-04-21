package run;
import collection.CollectionManager;
import commands.CommandInvoker;
import commands.ExecuteScriptCommand;
import exceptions.*;
import file.FileManager;
import file.InAndOut;
import file.StudyGroupFieldsReader;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Application {
    private CollectionManager collectionManager;
    private FileManager fileManager;
    private InAndOut inAndOut;
    private CommandInvoker commandInvoker;
    private StudyGroupFieldsReader studyGroupFieldsReader;
    private final Scanner scanner = new Scanner(System.in);

    public void start(String inputFile) {
        try {
            this.fileManager = new FileManager();
            this.inAndOut = new InAndOut();
            this.collectionManager = new CollectionManager(fileManager);
            this.studyGroupFieldsReader = new StudyGroupFieldsReader(inAndOut.getScanner());
            this.commandInvoker = new CommandInvoker();

            registerCommands();

            while (!loadCollection(inputFile)) {
                System.out.println("Попробуйте снова. Введите путь к JSON-файлу:");
                if (scanner.hasNextLine()) {
                    inputFile = scanner.nextLine().trim();
                } else {
                    System.out.println("Ввод завершён. Завершение программы.");
                    return;
                }
            }

            runCommandLoop();

        } catch (FileAccessException e) {
            inAndOut.printCommandError("Ошибка доступа к файлу: " + e.getMessage());
        } catch (CommandException e) {
            inAndOut.printCommandError("Ошибка выполнения команды: " + e.getMessage());
        } catch (NoSuchElementException e) {
            inAndOut.printCommandError("Ошибка ввода! Завершение программы.");
        } catch (Exception e) {
            inAndOut.printCommandError("Неизвестная ошибка: " + e.getMessage());
        }
    }

    private boolean loadCollection(String inputFile) {
        try {
            File file = new File(inputFile);
            if (!file.exists() || !file.canRead()) {
                throw new FileAccessException("Файл не найден или нет прав доступа.");
            }

            collectionManager.load(inputFile);
            InAndOut.printCommandText("Коллекция загружена из файла " + inputFile + "\n");
            return true;
        } catch (FileAccessException e) {
            inAndOut.printCommandError("Ошибка доступа к файлу: " + e.getMessage());
            return false;
        }
    }

    private void registerCommands() {
        commandInvoker.register("help", new commands.HelpCommand(commandInvoker));
        commandInvoker.register("info", new commands.InfoCommand(collectionManager));
        commandInvoker.register("show", new commands.ShowCommand(collectionManager));
        commandInvoker.register("add", new commands.AddCommand(collectionManager, studyGroupFieldsReader));
        commandInvoker.register("update", new commands.UpdateCommand(collectionManager, studyGroupFieldsReader));
        commandInvoker.register("remove_by_id", new commands.RemoveByIdCommand(collectionManager));
        commandInvoker.register("clear", new commands.ClearCommand(collectionManager));
        commandInvoker.register("save", new commands.SaveCommand(collectionManager, fileManager));
        commandInvoker.register("exit", new commands.ExitCommand());
        commandInvoker.register("execute_script", new ExecuteScriptCommand(collectionManager, studyGroupFieldsReader, fileManager, commandInvoker, new ExecuteScriptCommand.Script()));
        commandInvoker.register("add_if_max", new commands.AddIfMaxCommand(collectionManager, studyGroupFieldsReader));
        commandInvoker.register("add_if_min", new commands.AddIfMinCommand(collectionManager, studyGroupFieldsReader));
        commandInvoker.register("remove_greater", new commands.RemoveGreaterCommand(collectionManager, studyGroupFieldsReader));
        commandInvoker.register("remove_all_by_group_admin", new commands.RemoveAllByGroupAdminCommand(collectionManager));
        commandInvoker.register("filter_greater_than_group_admin", new commands.FilterGreaterThanGroupAdminCommand(collectionManager));
        commandInvoker.register("print_field_descending_semester_enum", new commands.PrintFieldDescendingSemesterEnumCommand(collectionManager));
    }

    private void runCommandLoop() {
        while (true) {
            try {
                InAndOut.printCommandText("\nВведите команду:\n");
                inAndOut.printPreamble();

                String line = inAndOut.readLine();

                if (line == null || line.trim().isEmpty()) {
                    System.out.println("Пустой ввод. Повторите:");
                    continue;
                }

                String[] parts = line.trim().split(" ", 2);
                String commandName = parts[0];
                String[] arguments = (parts.length > 1) ? new String[]{parts[1]} : new String[]{};

                commandInvoker.execute(commandName, arguments);

            } catch (NoSuchElementException e) {
                inAndOut.printCommandError("Ввод завершён (Ctrl+D). Повторите:");
            } catch (CommandException e) {
                inAndOut.printCommandError("Ошибка выполнения команды: " + e.getMessage());
            } catch (Exception e) {
                inAndOut.printCommandError("Неизвестная ошибка: " + e.getMessage());
            }
        }
    }
}