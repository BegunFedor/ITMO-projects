package commands;

import collection.CollectionManager;
import collection.StudyGroup;
import file.StudyGroupFieldsReader;

public class AddIfMaxCommand implements CommandWithArguments {
    private final CollectionManager collectionManager;
    private final StudyGroupFieldsReader studyGroupFieldsReader;
    private String[] commandArguments;

    public AddIfMaxCommand(CollectionManager collectionManager, StudyGroupFieldsReader studyGroupFieldsReader) {
        this.collectionManager = collectionManager;
        this.studyGroupFieldsReader = studyGroupFieldsReader;
    }

    @Override
    public void execute() {
        if (commandArguments == null || commandArguments.length == 0) {
            System.out.println("Введите данные для новой группы:");
            StudyGroup newGroup = studyGroupFieldsReader.readStudyGroup();

            if (collectionManager.isMax(newGroup)) {
                collectionManager.add(newGroup);
                System.out.println("Элемент успешно добавлен.");
            } else {
                System.out.println("Элемент не добавлен, так как он не максимальный.");
            }
        } else {
            System.err.println("Ошибка: команда не принимает аргументы. Просто введите 'add_if_max' и следуйте инструкциям.");
        }
    }

    @Override
    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию, если он превышает максимальный";
    }
}