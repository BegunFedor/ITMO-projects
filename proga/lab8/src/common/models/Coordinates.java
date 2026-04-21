package common.models;

import java.io.Serializable;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer x;
    private int y;

    public Coordinates(Integer x, int y) {
        if (x == null || x <= -32) {
            throw new IllegalArgumentException("X должен быть больше -32");
        }
        this.x = x;
        this.y = y;
    }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public Integer getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isValid() {
        return x != null && x > -32;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}