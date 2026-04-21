package commands;

import collection.CollectionManager;
import collection.StudyGroup;
import file.StudyGroupFieldsReader;


public class AddCommand implements Command {
    private final CollectionManager collectionManager;
    private final StudyGroupFieldsReader studyGroupFieldsReader;

    public AddCommand(CollectionManager collectionManager, StudyGroupFieldsReader studyGroupFieldsReader) {
        this.collectionManager = collectionManager;
        this.studyGroupFieldsReader = studyGroupFieldsReader;
    }

    @Override
    public void execute() {
        // Используем переданный StudyGroupFieldsReader для чтения данных
        StudyGroup newGroup = studyGroupFieldsReader.readStudyGroup();
        collectionManager.add(newGroup);
        System.out.println("Новая группа успешно добавлена.");
    }

    @Override
    public String getDescription() {
        return "добавить новый элемент в коллекцию";
    }
}