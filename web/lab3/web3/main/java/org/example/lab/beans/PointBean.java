package org.example.lab.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.OneToMany;
import org.example.lab.tools.PointService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Named
@SessionScoped
public class PointBean implements Serializable {

    private double x;
    private double y;
    private double r;

    private String pointType = "SPIDER";  // по умолчанию
    private Integer logsQuantity;
    private String bodyColor;

    @Inject
    private PointService pointService;
    private final List<Integer> xValues =
            Arrays.asList(-3, -2, -1, 0, 1, 2, 3, 4, 5);

    public void checkPoint() {
        try {
            pointService.processPoint(x, y, r);
        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, e.getMessage(), null);
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getR() { return r; }
    public void setR(double r) { this.r = r; }

    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }

    public Integer getLogsQuantity() { return logsQuantity; }
    public void setLogsQuantity(Integer logsQuantity) { this.logsQuantity = logsQuantity; }

    public String getBodyColor() { return bodyColor; }
    public void setBodyColor(String bodyColor) { this.bodyColor = bodyColor; }

    public List<Integer> getXValues() { return xValues; }
}