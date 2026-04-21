package server.commands;

import common.FileAccessException;
import common.network.CommandResponse;
import server.FileManager;
import server.CollectionManager;


public class SaveCommand implements ServerCommand {
    private final CollectionManager collectionManager;
    private final FileManager fileManager;

    public SaveCommand(CollectionManager collectionManager, FileManager fileManager) {
        this.collectionManager = collectionManager;
        this.fileManager = fileManager;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "сохранить коллекцию в файл (доступно только на сервере)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload) {
        try {
            String filePath = "default.json";
            fileManager.save(collectionManager.getCollection(), filePath);
            return new CommandResponse("Коллекция успешно сохранена в файл: " + filePath, true);
        } catch (FileAccessException e) {
            return new CommandResponse("Ошибка доступа к файлу: " + e.getMessage(), false);
        } catch (Exception e) {
            return new CommandResponse("Ошибка при сохранении данных: " + e.getMessage(), false);
        }
    }
}