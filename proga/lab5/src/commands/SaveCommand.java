package commands;

import collection.CollectionManager;
import exceptions.FileAccessException;
import file.FileManager;

public class SaveCommand implements Command {
    private CollectionManager collectionManager;
    private FileManager fileManager;

    public SaveCommand(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    @Override
    public void execute() {
        try {
            String filePath = "default.json";
            collectionManager.save(filePath);
            fileManager.save(collectionManager.getCollection(), filePath);
            System.out.println("Коллекция успешно сохранена в файл: " + filePath);
        } catch (FileAccessException e) {
            System.err.println("Ошибка доступа к файлу: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public String getDescription() {
        return "сохранить коллекцию в файл";
    }
}