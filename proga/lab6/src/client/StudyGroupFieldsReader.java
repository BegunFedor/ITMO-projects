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
        String name = readNonEmptyString("Введите название группы: ");
        Coordinates coordinates = readCoordinates();
        Integer studentsCount = readPositiveInt("Введите количество студентов (>0): ");
        FormOfEducation formOfEducation = readEnum("Выберите форму обучения", FormOfEducation.class);
        Semester semester = readEnum("Выберите семестр", Semester.class);
        Person groupAdmin = readPerson();

        return new StudyGroup(name, coordinates, studentsCount, formOfEducation, semester, groupAdmin);
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Поле не может быть пустым.");
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value > 0) return value;
                System.out.println("Значение должно быть больше 0.");
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное число.");
            }
        }
    }

    private Coordinates readCoordinates() {
        int x;
        while (true) {
            System.out.print("Введите координату X (больше -32): ");
            try {
                x = Integer.parseInt(scanner.nextLine().trim());
                if (x > -32) break;
                System.out.println("X должен быть больше -32.");
            } catch (NumberFormatException e) {
                System.out.println("Введите число.");
            }
        }

        System.out.print("Введите координату Y: ");
        int y = Integer.parseInt(scanner.nextLine().trim());

        return new Coordinates(x, y);
    }

    private <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        while (true) {
            System.out.print(prompt + " " + Arrays.toString(enumClass.getEnumConstants()) + ": ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException e) {
                System.out.println("Неверное значение. Повторите ввод.");
            }
        }
    }

    private Person readPerson() {
        String name = readNonEmptyString("Введите имя администратора: ");
        Double weight;
        while (true) {
            System.out.print("Введите вес администратора (>0): ");
            try {
                weight = Double.parseDouble(scanner.nextLine().trim());
                if (weight > 0) break;
                System.out.println("Вес должен быть больше 0.");
            } catch (NumberFormatException e) {
                System.out.println("Введите корректное число.");
            }
        }

        Color eyeColor = readEnum("Выберите цвет глаз", Color.class);
        Country nationality = readEnum("Выберите национальность", Country.class);

        return new Person(name,  weight, eyeColor, nationality);
    }
}