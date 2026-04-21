package file;

import collection.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StudyGroupFieldsReader {
    private final Scanner scanner;
    private BufferedReader fileReader;
    private boolean isReadingFromFile = false;

    public StudyGroupFieldsReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setBufferedReader(BufferedReader reader) {
        this.fileReader = reader;
        this.isReadingFromFile = true;
    }

    public void resetBufferedReader() {
        this.fileReader = null;
        this.isReadingFromFile = false;
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("Ошибка: поле не может быть пустым.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(readLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число.");
            }
        }
    }

    private Integer readPositiveIntOrNull(String prompt) {
        while (true) {
            System.out.print(prompt + " (или оставьте пустым): ");
            String input = readLine().trim();
            if (input.isEmpty()) return null;
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
                System.out.println("Ошибка: число должно быть больше 0.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число.");
            }
        }
    }

    private Coordinates readCoordinates() {
        int x;
        do {
            x = readInt("Введите координату X (больше -32): ");
            if (x <= -32) {
                System.out.println("Ошибка: X должно быть больше -32.");
            }
        } while (x <= -32);

        int y = readInt("Введите координату Y: ");
        return new Coordinates(x, y);
    }

    private <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        while (true) {
            System.out.print(prompt + " " + java.util.Arrays.toString(enumClass.getEnumConstants()) + ": ");
            String input = readLine().trim().toUpperCase();
            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: введите одно из значений.");
            }
        }
    }

    private Double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(readLine().trim());
                if (value > 0) return value;
                System.out.println("Ошибка: число должно быть больше 0.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число.");
            }
        }
    }

    private Person readPerson() {
        String name = readNonEmptyString("Введите имя администратора: ");
        Double weight = readPositiveDouble("Введите вес администратора (больше 0): ");
        Color eyeColor = readEnum("Выберите цвет глаз", Color.class);
        Country nationality = readEnum("Выберите страну", Country.class);
        return new Person(name, LocalDateTime.now(), weight, eyeColor, nationality);
    }

    public String readLine() {
        try {
            if (isReadingFromFile && fileReader != null) {
                String line = fileReader.readLine();
                if (line == null) {
                    resetBufferedReader();
                    return scanner.hasNextLine() ? scanner.nextLine() : null;
                }
                return line;
            } else {
                return scanner.hasNextLine() ? scanner.nextLine() : null;
            }
        } catch (IOException | NoSuchElementException e) {
            resetBufferedReader();
            return null;
        }
    }

    public StudyGroup readStudyGroup() {
        String name = readNonEmptyString("Введите название группы: ");
        Coordinates coordinates = readCoordinates();
        Integer studentsCount = readPositiveIntOrNull("Введите количество студентов (или оставьте пустым): ");
        FormOfEducation formOfEducation = readEnum("Выберите форму обучения", FormOfEducation.class);
        Semester semester = readEnum("Выберите семестр", Semester.class);
        Person groupAdmin = readPerson();

        return new StudyGroup(name, coordinates, studentsCount, formOfEducation, semester, groupAdmin);
    }
}