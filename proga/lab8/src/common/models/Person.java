package common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Person implements Serializable, Comparable<Person> {
    private static final long serialVersionUID = 1L;

    private String name; // Не null, не пустое// Может быть null
    private Double weight; // Не null, > 0
    private Color eyeColor; // Не null
    private Country nationality; // Не null

    public Person(String name, Double weight, Color eyeColor, Country nationality) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (weight == null || weight <= 0) {
            throw new IllegalArgumentException("Вес должен быть больше 0");
        }
        if (eyeColor == null) {
            throw new IllegalArgumentException("Цвет глаз не может быть null");
        }
        if (nationality == null) {
            throw new IllegalArgumentException("Национальность не может быть null");
        }

        this.name = name;
        this.weight = weight;
        this.eyeColor = eyeColor;
        this.nationality = nationality;
    }
    public int compareTo(Person other) {
        int result = Double.compare(this.weight, other.weight);
        if (result == 0) {
            result = this.name.compareToIgnoreCase(other.name);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public Double getWeight() {
        return weight;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public Country getNationality() {
        return nationality;
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && weight != null && weight > 0
                && eyeColor != null
                && nationality != null;
    }

    @Override
    public String toString() {
        return name + " (" + nationality + ")";
    }
}