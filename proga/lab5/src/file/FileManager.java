package file;

import collection.StudyGroup;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

public class FileManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public FileManager() {
    }
    public void save(LinkedHashMap<Integer, StudyGroup> collection, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(collection, writer);
            System.out.println("Коллекция успешно сохранена в файл: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении в файл: " + e.getMessage());
        }
    }
    public LinkedHashMap<Integer, StudyGroup> load(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Type type = new TypeToken<LinkedHashMap<Integer, StudyGroup>>(){}.getType();
            LinkedHashMap<Integer, StudyGroup> collection = gson.fromJson(reader, type);
            System.out.println("Данные успешно загружены из файла: " + filePath);
            return (collection != null) ? collection : new LinkedHashMap<>();
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден: " + filePath + ". Будет создана новая коллекция.");
            return new LinkedHashMap<>();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }}
