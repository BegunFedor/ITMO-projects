package org.example.lab.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "spider_points")
@DiscriminatorValue("SPIDER")
public class SpiderPointEntity extends PointEntity {

    @Column(name = "logs_quantity")
    private int logsQuantity;

    public SpiderPointEntity() {}

    public SpiderPointEntity(double x, double y, double r,
                             boolean hit, int logsQuantity) {
        super(x, y, r, hit);
        this.logsQuantity = logsQuantity;
    }

    public int getLogsQuantity() { return logsQuantity; }
    public void setLogsQuantity(int logsQuantity) { this.logsQuantity = logsQuantity; }
}