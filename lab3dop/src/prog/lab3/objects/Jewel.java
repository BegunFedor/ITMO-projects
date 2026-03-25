package prog.lab3.objects;

import prog.lab3.enums.LightLevel;

import java.util.Objects;

public class Jewel extends AbstractObject implements Describable{
    private final String description;
    private String location;


    public Jewel(String name, String description, String location) {
        super(name, LightLevel.DIM);
        this.description = description;
        this.location = location;
    }

    public Jewel(String name, String description) {
        this(name, description, "Unknown");
    }

    @Override
    public String getDescription() {
        return super.getName() + ":" + "Бриллиантовое ожерелье" + ", хранится в: " + description + ", местоположение: " + location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Jewel{" +
                "name='" + getName() + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", lightLevel=" + getLightLevel() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Jewel jewel = (Jewel) o;
        return Objects.equals(description, jewel.description) &&
                Objects.equals(location, jewel.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, location);
    }
}
