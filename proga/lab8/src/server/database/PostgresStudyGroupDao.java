package server.database;

import common.models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PostgresStudyGroupDao implements StudyGroupDao {
    private final Connection connection;

    public PostgresStudyGroupDao(Connection connection) {
        this.connection = connection;
    }

    public LinkedHashMap<Integer, StudyGroup> loadAll() {
        LinkedHashMap<Integer, StudyGroup> collection = new LinkedHashMap<>();
        String sql = "SELECT * FROM study_groups";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StudyGroup group = parseGroup(rs);
                collection.put(group.getId(), group);
            }

        } catch (SQLException e) {
            System.err.println("[DB] Ошибка загрузки коллекции: " + e.getMessage());
        }

        return collection;
    }

    private StudyGroup parseGroup(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int studentsCount = rs.getInt("students_count");
        FormOfEducation form = FormOfEducation.valueOf(rs.getString("form_of_education"));
        Semester semester = Semester.valueOf(rs.getString("semester"));
        String createdBy = rs.getString("created_by");

        String adminName = rs.getString("admin_name");
        double weight = rs.getDouble("admin_weight");
        Color eyeColor = Color.valueOf(rs.getString("eye_color"));
        Country nationality = Country.valueOf(rs.getString("nationality"));

        Coordinates coordinates = new Coordinates(x, y);
        Person admin = new Person(adminName, weight, eyeColor, nationality);
        StudyGroup group = new StudyGroup(name, coordinates, studentsCount, form, semester, admin);
        group.setId(id);
        group.setOwner(createdBy); // ✅ заменено
        return group;
    }

    @Override
    public int insert(StudyGroup group, String username) {
        String sql = "INSERT INTO study_groups " +
                "(name, x, y, students_count, form_of_education, semester, admin_name, admin_weight, eye_color, nationality, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, group.getName());
            stmt.setInt(2, group.getCoordinates().getX());
            stmt.setInt(3, group.getCoordinates().getY());
            stmt.setInt(4, group.getStudentsCount());
            stmt.setString(5, group.getFormOfEducation().name());
            stmt.setString(6, group.getSemesterEnum().name());
            stmt.setString(7, group.getGroupAdmin().getName());
            stmt.setDouble(8, group.getGroupAdmin().getWeight());
            stmt.setString(9, group.getGroupAdmin().getEyeColor().name());
            stmt.setString(10, group.getGroupAdmin().getNationality().name());
            stmt.setString(11, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                group.setId(id);
                group.setOwner(username); // ✅ заменено
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при добавлении группы: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean delete(int id, String username) {
        String sql = "DELETE FROM study_groups WHERE id = ? AND created_by = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка удаления: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int clearByUser(String username) {
        String sql = "DELETE FROM study_groups WHERE created_by = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка очистки групп пользователя: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean update(int id, StudyGroup newGroup, String username) {
        String sql = "UPDATE study_groups SET name=?, x=?, y=?, students_count=?, form_of_education=?, semester=?, " +
                "admin_name=?, admin_weight=?, eye_color=?, nationality=? " +
                "WHERE id=? AND created_by=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newGroup.getName());
            stmt.setInt(2, newGroup.getCoordinates().getX());
            stmt.setInt(3, newGroup.getCoordinates().getY());
            stmt.setInt(4, newGroup.getStudentsCount());
            stmt.setString(5, newGroup.getFormOfEducation().name());
            stmt.setString(6, newGroup.getSemesterEnum().name());
            stmt.setString(7, newGroup.getGroupAdmin().getName());
            stmt.setDouble(8, newGroup.getGroupAdmin().getWeight());
            stmt.setString(9, newGroup.getGroupAdmin().getEyeColor().name());
            stmt.setString(10, newGroup.getGroupAdmin().getNationality().name());
            stmt.setInt(11, id);
            stmt.setString(12, username);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка обновления: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isOwner(int id, String username) {
        String sql = "SELECT id FROM study_groups WHERE id = ? AND created_by = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при проверке владельца: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<StudyGroup> getAll() {
        List<StudyGroup> list = new ArrayList<>();
        String sql = "SELECT * FROM study_groups";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(parseGroup(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при получении всех групп: " + e.getMessage());
        }

        return list;
    }
}