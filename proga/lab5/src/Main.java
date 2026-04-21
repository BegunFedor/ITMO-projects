

import java.util.Scanner;
import run.*;
public class Main {
    public static void main(String[] args) {
        String inputFile;

        // Если путь передан через аргументы
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            inputFile = args[0].trim();
        } else {
            // Если путь не передан — просим ввести вручную
            System.out.println("Напишите адрес JSON-файла:");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                if (!scanner.hasNextLine()) {
                    System.out.println("Обнаружен конец ввода (Ctrl + D). Завершение программы.");
                    return;
                }

                inputFile = scanner.nextLine().trim();

                if (inputFile.isEmpty()) {
                    System.out.println("Ошибка: файл не указан. Повторите ввод:");
                } else {
                    break;
                }
            }
        }

        Application app = new Application();
        app.start(inputFile);
    }
}