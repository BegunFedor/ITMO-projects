package common.models;

import java.io.Serializable;
import java.util.Objects;

public class StudyGroup implements Serializable, Comparable<StudyGroup> {
    private static final long serialVersionUID = 1L;
    private static transient int nextId = 1;

    private int id;
    private String name;
    private Coordinates coordinates;
    private Integer studentsCount;
    private FormOfEducation formOfEducation;
    private Semester semesterEnum;
    private Person groupAdmin;

    public StudyGroup(String name, Coordinates coordinates, Integer studentsCount,
                      FormOfEducation formOfEducation, Semester semesterEnum, Person groupAdmin) {
        this.id = nextId++;
        this.name = Objects.requireNonNull(name, "Имя не может быть null");
        this.coordinates = Objects.requireNonNull(coordinates, "Координаты не могут быть null");
        this.studentsCount = studentsCount;
        this.formOfEducation = Objects.requireNonNull(formOfEducation, "Форма обучения не может быть null");
        this.semesterEnum = semesterEnum;
        this.groupAdmin = Objects.requireNonNull(groupAdmin, "Администратор группы не может быть null");
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть больше 0!");
        }
        this.id = id;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Integer getStudentsCount() { return studentsCount; }
    public FormOfEducation getFormOfEducation() { return formOfEducation; }
    public Semester getSemesterEnum() { return semesterEnum; }
    public Person getGroupAdmin() { return groupAdmin; }

    @Override
    public int compareTo(StudyGroup other) {
        return this.name.compareToIgnoreCase(other.name); // сортировка по названию
    }

    @Override
    public String toString() {
        return "StudyGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", studentsCount=" + studentsCount +
                ", formOfEducation=" + formOfEducation +
                ", semesterEnum=" + semesterEnum +
                ", groupAdmin=" + groupAdmin +
                '}';
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && coordinates != null && coordinates.isValid()
                && studentsCount != null && studentsCount > 0
                && formOfEducation != null
                && semesterEnum != null
                && groupAdmin != null && groupAdmin.isValid();
    }
}