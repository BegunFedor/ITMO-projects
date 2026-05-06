package org.example.lab.validation;

import java.util.List;

public class InputValidator {

    private static final int X_MIN = -3;
    private static final int X_MAX = 5;

    private static final int Y_MIN = -5;
    private static final int Y_MAX = 5;

    private static final List<Double> ALLOWED_R =
            List.of(1.0, 1.5, 2.0, 2.5, 3.0);

    public static boolean canParse(String xStr, String yStr, String rStr) {
        try {
            Double.parseDouble(xStr);
            Double.parseDouble(yStr);
            Double.parseDouble(rStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void validate(double x, double y, double r) {

        if (Double.isNaN(x) || Double.isInfinite(x))
            throw new IllegalArgumentException("Некорректный X");

        if (Double.isNaN(y) || Double.isInfinite(y))
            throw new IllegalArgumentException("Некорректный Y");

        if (Double.isNaN(r) || Double.isInfinite(r))
            throw new IllegalArgumentException("Некорректный R");

        if (x < X_MIN || x > X_MAX)
            throw new IllegalArgumentException("X должен быть в диапазоне [-3; 5]");

        if (y < Y_MIN || y > Y_MAX)
            throw new IllegalArgumentException("Y должен быть в диапазоне [-5; 5]");

        if (!ALLOWED_R.contains(r))
            throw new IllegalArgumentException("R должен быть одним из: " + ALLOWED_R);
    }
}
