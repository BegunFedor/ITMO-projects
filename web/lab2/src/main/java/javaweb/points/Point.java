package javaweb.points;

import java.util.Date;

public class Point {
    private double x;
    private double y;
    private double r;
    private boolean hit;
    private Date timestamp;
    private long executionTime;
    private String timezone;

    public Point(double x, double y, double r) {

        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = checkHit();
        this.timestamp = new Date();
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    private boolean checkHit() {
        boolean rectangle = (x <= 0 && y >= 0 && x >= -r / 2 && y <= r);
        boolean circle = (x >= 0 && y >= 0 && (x * x + y * y <= (r / 2) * (r / 2)));
        boolean triangle = (x >= 0 && y <= 0 && y >= x - r);
        return rectangle || circle || triangle;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getR() {
        return r;
    }

    public boolean isHit() {
        return hit;
    }

    public String getTimezone() { return timezone; }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}