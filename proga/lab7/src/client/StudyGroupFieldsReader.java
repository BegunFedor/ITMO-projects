package client;

import common.models.*;

import java.util.Arrays;
import java.util.Scanner;

public class StudyGroupFieldsReader {
    private final Scanner scanner;

    public StudyGroupFieldsReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public StudyGroup readStudyGroup() {
        String name = readNonEmpty("Название группы: ");
        Coordinates coords = readCoordinates();
        int count = readPositiveInt("Количество студентов (>0): ");
        FormOfEducation form = readEnum("Форма обучения", FormOfEducation.class);
        Semester sem = readEnum("Семестр", Semester.class);
        Person admin = readPerson();

        return new StudyGroup(name, coords, count, form, sem, admin);
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Поле не может быть пустым.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = Integer.parseInt(scanner.nextLine());
                if (val > 0) return val;
                System.out.println("Значение должно быть > 0.");
            } catch (Exception e) {
                System.out.println("Введите корректное число.");
            }
        }
    }

    private Coordinates readCoordinates() {
        int x;
        while (true) {
            System.out.print("Координата X (> -32): ");
            try {
                x = Integer.parseInt(scanner.nextLine());
                if (x > -32) break;
                System.out.println("X должен быть > -32.");
            } catch (Exception e) {
                System.out.println("Некорректный ввод.");
            }
        }

        System.out.print("Координата Y: ");
        int y = Integer.parseInt(scanner.nextLine());
        return new Coordinates(x, y);
    }

    private <T extends Enum<T>> T readEnum(String prompt, Class<T> clazz) {
        while (true) {
            System.out.print(prompt + " " + Arrays.toString(clazz.getEnumConstants()) + ": ");
            try {
                return Enum.valueOf(clazz, scanner.nextLine().trim().toUpperCase());
            } catch (Exception e) {
                System.out.println("Неверное значение.");
            }
        }
    }

    public Person readPerson() {
        String name = readNonEmpty("Имя администратора: ");
        double weight;
        while (true) {
            try {
                System.out.print("Вес администратора (>0): ");
                weight = Double.parseDouble(scanner.nextLine());
                if (weight > 0) break;
                System.out.println("Вес должен быть больше 0.");
            } catch (Exception e) {
                System.out.println("Некорректный ввод.");
            }
        }

        Color eyeColor = readEnum("Цвет глаз", Color.class);
        Country nationality = readEnum("Национальность", Country.class);
        return new Person(name, weight, eyeColor, nationality);
    }
}