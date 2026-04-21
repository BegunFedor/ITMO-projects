package collection;

public class Coordinates {
    private Integer x;
    private int y;

    public Coordinates(Integer x, int y) {
        if (x == null || x <= -32) {
            throw new IllegalArgumentException("X должен быть больше -32");
        }
        this.x = x;
        this.y = y;
    }

    public Integer getX() { return x; }
    public int getY() { return y; }

    public boolean isValid() {
        return x > -32; // Условие из твоего поля
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}