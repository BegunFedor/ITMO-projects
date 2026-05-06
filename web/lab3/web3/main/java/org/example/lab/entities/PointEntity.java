package org.example.lab.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "points")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "point_type")
public abstract class PointEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "x_coord", nullable = false)
    private double x;

    @Column(name = "y_coord", nullable = false)
    private double y;

    @Column(name = "radius", nullable = false)
    private double r;

    @Column(name = "hit_result", nullable = false)
    private boolean hit;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date timestamp = new Date();

    @Column(name = "execution_time")
    private long executionTime;

    public PointEntity() {}

    public PointEntity(double x, double y, double r, boolean hit) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.hit = hit;
    }

    @Transient
    public String getType() {
        if (this instanceof SpiderPointEntity) return "Паук";
        if (this instanceof AntPointEntity) return "Муравей";
        return "Точка";
    }

    @Transient
    public String getExtra() {
        if (this instanceof SpiderPointEntity spider) {
            return "Лапы: " + spider.getLogsQuantity();
        }
        if (this instanceof AntPointEntity ant) {
            return "Цвет: " + ant.getBodyColor();
        }
        return "-";
    }

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getR() { return r; }
    public void setR(double r) { this.r = r; }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
}