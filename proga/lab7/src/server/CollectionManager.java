package server;

import common.models.Person;
import common.models.StudyGroup;
import server.database.StudyGroupDao;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CollectionManager {
    private final Map<Integer, StudyGroup> studyGroups = new ConcurrentHashMap<>();
    private final Date initializationDate;
    private final StudyGroupDao studyGroupDao;

    public CollectionManager(StudyGroupDao studyGroupDao) {
        this.studyGroupDao = studyGroupDao;
        this.initializationDate = new Date();
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        studyGroups.clear();
        studyGroupDao.getAll().forEach(group -> studyGroups.put(group.getId(), group));
    }

    public String info() {
        return "Тип коллекции: ConcurrentHashMap\n" +
                "Дата инициализации: " + initializationDate + "\n" +
                "Количество элементов: " + studyGroups.size();
    }

    public String showFormatted() {
        if (studyGroups.isEmpty()) return "Коллекция пуста.";
        return formatGroups(studyGroups.values());
    }

    public boolean add(StudyGroup group, String username) {
        int newId = studyGroupDao.insert(group, username);
        if (newId != -1) {
            group.setId(newId);
            studyGroups.put(newId, group);
            return true;
        }
        return false;
    }

    public boolean update(int id, StudyGroup newGroup, String username) {
        if (studyGroupDao.update(id, newGroup, username)) {
            newGroup.setId(id);
            studyGroups.put(id, newGroup);
            return true;
        }
        return false;
    }

    public boolean removeById(int id, String username) {
        if (studyGroupDao.delete(id, username)) {
            studyGroups.remove(id);
            return true;
        }
        return false;
    }

    public boolean clear(String username) {
        int deletedCount = studyGroupDao.clearByUser(username);  // ✅ вызов метода с ()
        if (deletedCount > 0) {
            studyGroups.entrySet().removeIf(entry -> username.equals(entry.getValue().getOwner()));
            return true;
        }
        return false;
    }

    public boolean isMax(StudyGroup newGroup) {
        return studyGroups.values().stream().max(StudyGroup::compareTo)
                .map(group -> newGroup.compareTo(group) > 0).orElse(true);
    }

    public boolean isMin(StudyGroup newGroup) {
        return studyGroups.values().stream().min(StudyGroup::compareTo)
                .map(group -> newGroup.compareTo(group) < 0).orElse(true);
    }

    public int removeGreater(StudyGroup referenceGroup, String username) {
        List<Integer> toRemove = studyGroups.values().stream()
                .filter(group -> group.compareTo(referenceGroup) > 0)
                .filter(group -> username.equals(group.getOwner()))
                .map(StudyGroup::getId)
                .toList();

        toRemove.forEach(id -> removeById(id, username));

        return toRemove.size(); // возвращаем количество удалённых элементов
    }

    public int removeAllByGroupAdmin(Person admin, String username) {
        List<Integer> toRemove = studyGroups.values().stream()
                .filter(g -> g.getGroupAdmin().equals(admin))
                .filter(g -> username.equals(g.getOwner()))
                .map(StudyGroup::getId)
                .toList();

        int count = 0;
        for (int id : toRemove) {
            if (removeById(id, username)) count++;
        }
        return count;
    }

    public String filterGreaterThanGroupAdmin(Person admin) {
        return studyGroups.values().stream()
                .filter(group -> group.getGroupAdmin().compareTo(admin) > 0)
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }

    public String printFieldDescendingSemesterEnum() {
        return studyGroups.values().stream()
                .map(StudyGroup::getSemesterEnum)
                .filter(Objects::nonNull)
                .sorted(Comparator.reverseOrder())
                .map(Enum::toString)
                .collect(Collectors.joining("\n"));
    }

    private String formatGroups(Collection<StudyGroup> groups) {
        return groups.stream()
                .map(StudyGroup::toString)
                .collect(Collectors.joining("\n"));
    }

    public Map<Integer, StudyGroup> getCollection() {
        return studyGroups;
    }
}