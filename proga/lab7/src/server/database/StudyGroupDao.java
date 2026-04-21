package server.database;

import common.models.StudyGroup;

import java.util.List;

public interface StudyGroupDao {
    int insert(StudyGroup group, String owner);
    boolean update(int id, StudyGroup group, String owner);
    boolean delete(int id, String owner);
    List<StudyGroup> getAll();
    boolean isOwner(int id, String username);

    // Возвращает количество удалённых групп
    int clearByUser(String username);
}