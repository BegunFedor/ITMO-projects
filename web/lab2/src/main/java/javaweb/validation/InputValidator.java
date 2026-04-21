package javaweb.validation;

public class InputValidator {


    public static boolean isValid(double x, double y, double r) {
        return (x >= -2 && x <= 2) &&
                (y >= -3 && y <= 3) &&
                (r == 1 || r == 1.5 || r == 2 || r == 2.5 || r == 3);
    }
    public static boolean canParse(String xStr, String yStr, String rStr) {
        try {
            Double.parseDouble(xStr);
            Double.parseDouble(yStr);
            Double.parseDouble(rStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}