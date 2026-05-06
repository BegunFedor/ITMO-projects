package org.example.lab.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.example.lab.beans.PointBean;
import org.example.lab.entities.AntPointEntity;
import org.example.lab.entities.PointEntity;
import org.example.lab.entities.SpiderPointEntity;
import org.example.lab.validation.InputValidator;

import java.util.List;
import java.util.Random;

@ApplicationScoped
public class PointService {

    @Inject
    private ApplicationService applicationService;

    @Inject
    private PointBean pointBean;

    public void processPoint(double x, double y, double r) {

        long execStart = System.nanoTime();

        InputValidator.validate(x, y, r);

        boolean hit = checkHit(x, y, r);
        PointEntity entity;

        switch (pointBean.getPointType()) {

            case "SPIDER" -> {
                Integer logs = pointBean.getLogsQuantity();
                if (logs == null) logs = new Random().nextInt(10) + 1;
                entity = new SpiderPointEntity(x, y, r, hit, logs);
            }

            case "ANT" -> {
                String color = pointBean.getBodyColor();
                if (color == null || color.isBlank()) color = getRandomColor();
                entity = new AntPointEntity(x, y, r, hit, color);
            }

            default ->
                    throw new IllegalArgumentException("Unknown type: " + pointBean.getPointType());
        }

        long execEnd = System.nanoTime();

        entity.setExecutionTime(execEnd - execStart);

        applicationService.savePoint(entity);

        pointBean.setBodyColor(null);
        pointBean.setLogsQuantity(null);
    }

    private String getRandomColor() {
        String[] colors = {"red", "black", "orange", "green"};
        return colors[new Random().nextInt(colors.length)];
    }

    private boolean checkHit(double x, double y, double r) {

        boolean rect = (x >= -r/2 && x <= 0) &&
                (y >= -r && y <= 0);

        boolean triangle = (x >= 0 && x <= r) &&
                (y >= 0 && y <= (-0.5 * x + r/2));

        boolean circle = (x >= 0 && y <= 0 &&
                (x*x + y*y <= r*r));

        return rect || triangle || circle;
    }

    public List<PointEntity> getAllPoints() {
        return applicationService.getAllPoints();
    }
}