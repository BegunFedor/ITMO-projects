package server.commands;

import common.FileAccessException;
import common.models.StudyGroup;
import common.network.CommandResponse;
import server.CollectionManager;
import server.FileManager;

import java.util.LinkedHashMap;

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
        return "сохранить коллекцию в файл (только серверная команда)";
    }

    @Override
    public CommandResponse execute(String[] args, Object payload, String username) {
        try {
            String filePath = "default.json";

            // Преобразуем Map в LinkedHashMap
            LinkedHashMap<Integer, StudyGroup> dataToSave = new LinkedHashMap<>(collectionManager.getCollection());

            fileManager.save(dataToSave, filePath);
            return new CommandResponse("Коллекция сохранена в файл: " + filePath, true);
        } catch (FileAccessException e) {
            return new CommandResponse("Ошибка доступа к файлу: " + e.getMessage(), false);
        } catch (Exception e) {
            return new CommandResponse("Ошибка при сохранении: " + e.getMessage(), false);
        }
    }
}