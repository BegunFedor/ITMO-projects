package collection;

import java.util.*;
import file.FileManager;

public class CollectionManager {
    private final LinkedHashMap<Integer, StudyGroup> studyGroups; //

    private final Date initializationDate;
    private final FileManager fileManager;
    private static int nextId = 1;

    public CollectionManager(FileManager fileManager) {
        this.studyGroups = new LinkedHashMap<>(); //
        this.initializationDate = new Date();
        this.fileManager = fileManager;
    }


    public LinkedHashMap<Integer, StudyGroup> getCollection() { //
        return studyGroups;
    }

    public Date getInitializationDate() {
        return initializationDate;
    }

    public int generateNextId() {
        return nextId++;
    }


    public String info() {
        return "Тип коллекции: LinkedHashMap\n" +
                "Дата инициализации: " + initializationDate + "\n" +
                "Количество элементов: " + studyGroups.size();
    }
    public void removeGreater(StudyGroup referenceGroup) {
        if (studyGroups.isEmpty()) {
            System.out.println("Коллекция пуста, удалять нечего.");
            return;
        }


        int initialSize = studyGroups.size();


        studyGroups.values().removeIf(group -> group.compareTo(referenceGroup) > 0);


        if (studyGroups.size() < initialSize) {
            System.out.println("Удалены все элементы, превышающие заданный.");
        } else {
            System.out.println("Не найдено элементов, превышающих заданный.");
        }
    }


    public String show() {
        if (studyGroups.isEmpty()) {
            return "Коллекция пуста.";
        }
        StringBuilder result = new StringBuilder();
        studyGroups.values().forEach(group -> result.append(group).append("\n"));
        return result.toString();
    }

    public String formatGroups(Collection<StudyGroup> groups) {
        if (groups.isEmpty()) return "Коллекция пуста.";

        String[] headers = {
                "ID", "Название", "Студенты", "Форма", "Семестр",
                "Коорд X", "Коорд Y", "Админ", "Вес", "Цвет глаз", "Национальность"
        };
        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) columnWidths[i] = headers[i].length();

        for (StudyGroup group : groups) {
            columnWidths[0] = Math.max(columnWidths[0], String.valueOf(group.getId()).length());
            columnWidths[1] = Math.max(columnWidths[1], group.getName().length());
            columnWidths[2] = Math.max(columnWidths[2], String.valueOf(group.getStudentsCount()).length());
            columnWidths[3] = Math.max(columnWidths[3], group.getFormOfEducation().toString().length());
            columnWidths[4] = Math.max(columnWidths[4], group.getSemesterEnum().toString().length());
            columnWidths[5] = Math.max(columnWidths[5], String.valueOf(group.getCoordinates().getX()).length());
            columnWidths[6] = Math.max(columnWidths[6], String.valueOf(group.getCoordinates().getY()).length());
            columnWidths[7] = Math.max(columnWidths[7], group.getGroupAdmin().getName().length());
            columnWidths[8] = Math.max(columnWidths[8], String.valueOf(group.getGroupAdmin().getWeight()).length());
            columnWidths[9] = Math.max(columnWidths[9], group.getGroupAdmin().getEyeColor().toString().length());
            columnWidths[10] = Math.max(columnWidths[10], group.getGroupAdmin().getNationality().toString().length());
        }

        StringBuilder table = new StringBuilder();
        table.append("|");
        for (int i = 0; i < headers.length; i++) {
            table.append(String.format(" %-" + columnWidths[i] + "s |", headers[i]));
        }
        table.append("\n|");
        for (int width : columnWidths) {
            table.append("-".repeat(width + 2)).append("|");
        }
        table.append("\n");

        for (StudyGroup group : groups) {
            Person admin = group.getGroupAdmin();
            table.append(String.format("| %-" + columnWidths[0] + "d |", group.getId()));
            table.append(String.format(" %-" + columnWidths[1] + "s |", group.getName()));
            table.append(String.format(" %-" + columnWidths[2] + "s |", group.getStudentsCount()));
            table.append(String.format(" %-" + columnWidths[3] + "s |", group.getFormOfEducation()));
            table.append(String.format(" %-" + columnWidths[4] + "s |", group.getSemesterEnum()));
            table.append(String.format(" %-" + columnWidths[5] + "d |", group.getCoordinates().getX()));
            table.append(String.format(" %-" + columnWidths[6] + "d |", group.getCoordinates().getY()));
            table.append(String.format(" %-" + columnWidths[7] + "s |", admin.getName()));
            table.append(String.format(" %-" + columnWidths[8] + ".1f |", admin.getWeight()));
            table.append(String.format(" %-" + columnWidths[9] + "s |", admin.getEyeColor()));
            table.append(String.format(" %-" + columnWidths[10] + "s |", admin.getNationality()));
            table.append("\n");
        }

        return table.toString();
    }

    public String showFormatted() {
        return formatGroups(studyGroups.values());
    }


    public void add(StudyGroup group) {
        group.setId(generateNextId());
        studyGroups.put(group.getId(), group);
    }


    public boolean update(int id, StudyGroup newGroup) {
        if (studyGroups.containsKey(id)) {
            newGroup.setId(id);
            studyGroups.put(id, newGroup);
            return true;
        }
        return false;
    }


    public boolean removeById(int id) {
        return studyGroups.remove(id) != null;
    }


    public boolean isMax(StudyGroup newGroup) {
        return studyGroups.values().stream().max(StudyGroup::compareTo)
                .map(group -> newGroup.compareTo(group) > 0).orElse(true);
    }


    public boolean isMin(StudyGroup newGroup) {
        return studyGroups.values().stream().min(StudyGroup::compareTo)
                .map(group -> newGroup.compareTo(group) < 0).orElse(true);
    }


    public void clear() {
        studyGroups.clear();
    }


    public void save(String filename) {
        fileManager.save(studyGroups, filename); //
    }


    public void load(String filename) {
        LinkedHashMap<Integer, StudyGroup> loadedCollection = fileManager.load(filename);
        if (loadedCollection != null) {
            int validCount = 0;
            studyGroups.clear();

            for (Map.Entry<Integer, StudyGroup> entry : loadedCollection.entrySet()) {
                StudyGroup group = entry.getValue();
                if (group != null && group.isValid()) {
                    studyGroups.put(entry.getKey(), group);
                    validCount++;
                } else {
                    System.err.println("Невалидный элемент (ID " + entry.getKey() + ") пропущен при загрузке.");
                }
            }

            nextId = studyGroups.keySet().stream().max(Integer::compareTo).orElse(0) + 1;

            System.out.println("Загружено " + validCount + " валидных элементов из " + loadedCollection.size());
        }
    }


    public String filterGreaterThanStudentsCount(int studentsCount) {
        StringBuilder result = new StringBuilder();
        studyGroups.values().stream()
                .filter(group -> group.getStudentsCount() != null && group.getStudentsCount() > studentsCount)
                .forEach(group -> result.append(group).append("\n"));

        return result.length() > 0 ? result.toString() : "Элементы не найдены.";
    }

    public String printFieldDescendingSemesterEnum() {
        return studyGroups.values().stream()
                .map(StudyGroup::getSemesterEnum)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .map(Enum::toString)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("Значения semesterEnum отсутствуют.");
    }
}