package org.example.lab.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.ManyToOne;
import org.example.lab.entities.*;
import org.example.lab.tools.PointService;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Named
@ApplicationScoped
public class ResultsBean implements Serializable {

    @Inject
    private PointService pointService;

    public List<PointEntity> getAllPoints() {
        return pointService.getAllPoints();
    }

    public boolean isSpider(PointEntity p) {
        return p instanceof SpiderPointEntity;
    }

    public boolean isAnt(PointEntity p) {
        return p instanceof AntPointEntity;
    }

    public String extraInfo(PointEntity p) {
        if (p instanceof SpiderPointEntity sp) {
            return "Ног: " + sp.getLogsQuantity();
        }
        if (p instanceof AntPointEntity ant) {
            return "Цвет: " + ant.getBodyColor();
        }
        return "";
    }

    public String formatTime(Date timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(timestamp);
    }

    public String formatExecutionTime(long executionTime) {
        return String.format("%.3f", executionTime / 1_000_000.0);
    }
}