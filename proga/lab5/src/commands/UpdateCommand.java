package commands;

import collection.*;
import file.StudyGroupFieldsReader;

public class UpdateCommand implements CommandWithArguments {
    private  CollectionManager collectionManager;
    private  StudyGroupFieldsReader studyGroupFieldsReader;
    private String[] commandArguments;

    public UpdateCommand(CollectionManager collectionManager, StudyGroupFieldsReader studyGroupFieldsReader) {
        this.collectionManager = collectionManager;
        this.studyGroupFieldsReader = studyGroupFieldsReader;
    }

    @Override
    public void execute() {
        try {
            int id = Integer.parseInt(commandArguments[0]);

            if (!collectionManager.getCollection().containsKey(id)) {
                System.out.println("Элемент с таким ID не найден.");
                return;
            }

            System.out.println("Введите новые данные для элемента с ID " + id + ":");
            StudyGroup newGroup = studyGroupFieldsReader.readStudyGroup();

            if (collectionManager.update(id, newGroup)) {
                System.out.println("Элемент успешно обновлен.");
            } else {
                System.out.println("Ошибка при обновлении элемента.");
            }

        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.err.println("Ошибка: некорректный ID.");
        }
    }

    @Override
    public void getCommandArguments(String[] commandArguments) {
        this.commandArguments = commandArguments;
    }

    @Override
    public String getDescription() {
        return "обновить элемент коллекции по ID";
    }
}