package org.example.lab.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "ant_points")
@DiscriminatorValue("ANT")
public class AntPointEntity extends PointEntity {

    @Column(name = "body_color")
    private String bodyColor;

    public AntPointEntity() {}

    public AntPointEntity(double x, double y, double r,
                          boolean hit, String bodyColor) {
        super(x, y, r, hit);
        this.bodyColor = bodyColor;
    }

    public String getBodyColor() { return bodyColor; }
    public void setBodyColor(String bodyColor) { this.bodyColor = bodyColor; }
}