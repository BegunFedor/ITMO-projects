package commands;

import collection.CollectionManager;
import exceptions.RecoursiveCallException;
import file.FileManager;
import file.StudyGroupFieldsReader;

import java.io.*;
import java.util.ArrayList;


public class ExecuteScriptCommand implements CommandWithArguments {
    private String[] commandArguments;
    private final CollectionManager collectionManager;
    private final StudyGroupFieldsReader studyGroupFieldsReader;
    private final FileManager fileManager;
    private final CommandInvoker commandInvoker;
    private final Script script;
    private String scriptPath;

    public ExecuteScriptCommand(CollectionManager collectionManager, StudyGroupFieldsReader studyGroupFieldsReader,
                                FileManager fileManager, CommandInvoker commandInvoker, Script script) {
        this.collectionManager = collectionManager;
        this.studyGroupFieldsReader = studyGroupFieldsReader;
        this.fileManager = fileManager;
        this.commandInvoker = commandInvoker;
        this.script = script;
    }


    @Override
    public void execute() {
        try {
            if (commandArguments == null || commandArguments.length != 1) {
                throw new IllegalArgumentException("Ошибка: команда `execute_script` требует один аргумент - путь к файлу.");
            }

            scriptPath = commandArguments[0];

            // Проверка на рекурсивный вызов
            if (script.scriptPaths.contains(scriptPath)) {
                throw new RecoursiveCallException("Ошибка: рекурсивный вызов `execute_script` обнаружен! Файл `" + scriptPath + "` уже выполняется.");
            }

            script.putScript(scriptPath); //чтобы отследить вложенные вызовы и не попасть в цикл
            File scriptFile = new File(scriptPath);

            // Проверяем доступность файла
            if (!scriptFile.exists() || !scriptFile.canRead()) {
                throw new IOException("Ошибка: файл `" + scriptPath + "` не найден или нет прав доступа.");
            }

            FileInputStream fileInputStream = new FileInputStream(scriptFile);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Переключаем ввод в `StudyGroupFieldsReader` на файл
            studyGroupFieldsReader.setBufferedReader(bufferedReader);

            System.out.println("> Выполняем команды из файла: " + scriptPath);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    System.out.println("> Выполняем команду: " + line);

                    //  Разбиваем строку на команду и аргумент
                    String[] parts = line.split(" ", 2);
                    String commandName = parts[0];
                    String[] args = (parts.length > 1) ? new String[]{parts[1]} : new String[]{};

                    commandInvoker.execute(commandName, args);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println("Ошибка: файл `" + scriptPath + "` не найден.");
        } catch (IOException e) {
            System.err.println("Ошибка при доступе к файлу `" + scriptPath + "`: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (RecoursiveCallException e) {
            System.err.println(e.getMessage());
        } finally {
            studyGroupFieldsReader.setBufferedReader(new BufferedReader(new InputStreamReader(System.in)));
            script.removeScript(scriptPath);
        }
    }

    @Override
    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    @Override
    public String getDescription() {
        return "Считать и выполнить команды из указанного файла.";
    }

    public static class Script {
        private final ArrayList<String> scriptPaths = new ArrayList<>();

        public void putScript(String scriptPath) {
            scriptPaths.add(scriptPath);
        }

        public void removeScript(String scriptPath) {
            scriptPaths.remove(scriptPath);
        }
    }
}