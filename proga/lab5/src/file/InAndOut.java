package file;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class InAndOut {
    private Scanner scanner;
    private final PrintStream out;
    private final PrintStream err;

    public InAndOut() {
        this(new NonCloseableInputStream(System.in), System.out, System.err);
    }

    public InAndOut(InputStream input, PrintStream output, PrintStream error) {
        this.scanner = new Scanner(input, "UTF-8");
        this.out = output;
        this.err = error;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public static void printCommandText(String str) {
        System.out.print(str);
    }

    public void printCommandError(String str) {
        this.err.println(str);
    }

    public void printPreamble() {
        this.out.print(">");
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void resetScanner() {
        if (this.scanner != null) {
            this.scanner.close();
        }
        this.scanner = new Scanner(System.in); // Новый сканер
    }

    public String readLine() {
        try {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            } else {
                return null; // Ctrl+D
            }
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }
}