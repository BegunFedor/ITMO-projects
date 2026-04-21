package common.models;

import java.io.Serializable;
import java.util.Objects;

public class StudyGroup implements Serializable, Comparable<StudyGroup> {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private Coordinates coordinates;
    private Integer studentsCount;
    private FormOfEducation formOfEducation;
    private Semester semesterEnum;
    private Person groupAdmin;
    private String owner; // пользователь, создавший группу

    public StudyGroup(String name, Coordinates coordinates, Integer studentsCount,
                      FormOfEducation formOfEducation, Semester semesterEnum, Person groupAdmin) {
        this.name = Objects.requireNonNull(name, "Имя не может быть null");
        this.coordinates = Objects.requireNonNull(coordinates, "Координаты не могут быть null");
        this.studentsCount = Objects.requireNonNull(studentsCount, "studentsCount не может быть null");
        this.formOfEducation = Objects.requireNonNull(formOfEducation, "Форма обучения не может быть null");
        this.semesterEnum = Objects.requireNonNull(semesterEnum, "Семестр не может быть null");
        this.groupAdmin = Objects.requireNonNull(groupAdmin, "Администратор не может быть null");
    }

    public int getId() { return id; }

    public void setId(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID должен быть положительным");
        this.id = id;
    }


    public String getName() { return name; }

    public Coordinates getCoordinates() { return coordinates; }

    public Integer getStudentsCount() { return studentsCount; }

    public FormOfEducation getFormOfEducation() { return formOfEducation; }

    public Semester getSemesterEnum() { return semesterEnum; }

    public Person getGroupAdmin() { return groupAdmin; }

    public String getOwner() { return owner; }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int compareTo(StudyGroup other) {
        return this.name.compareToIgnoreCase(other.name);
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
                ", owner='" + owner + '\'' +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyGroup)) return false;
        StudyGroup that = (StudyGroup) o;
        return id == that.id && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}