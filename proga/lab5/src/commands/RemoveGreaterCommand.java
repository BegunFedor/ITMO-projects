package commands;

import collection.CollectionManager;
import collection.StudyGroup;
import file.StudyGroupFieldsReader;

public class RemoveGreaterCommand implements CommandWithArguments {
    private final CollectionManager collectionManager;
    private final StudyGroupFieldsReader studyGroupFieldsReader;
    private StudyGroup referenceGroup;

    public RemoveGreaterCommand(CollectionManager collectionManager, StudyGroupFieldsReader studyGroupFieldsReader) {
        this.collectionManager = collectionManager;
        this.studyGroupFieldsReader = studyGroupFieldsReader;
    }

    @Override
    public void execute() {
        if (referenceGroup == null) {
            System.out.println("Ошибка: группа не была задана.");
            return;
        }
        collectionManager.removeGreater(referenceGroup);
    }

    @Override
    public void getCommandArguments(String[] commandArguments) {
        try {
            System.out.println("Введите данные для группы, с которой будет сравнение:");
            referenceGroup = studyGroupFieldsReader.readStudyGroup();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "удаляет все элементы, превышающие заданный.";
    }
}