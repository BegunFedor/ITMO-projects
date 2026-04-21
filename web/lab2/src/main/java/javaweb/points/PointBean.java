package javaweb.points;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Named
@ApplicationScoped
public class PointBean implements Serializable {
    private final List<Point> points = Collections.synchronizedList(new ArrayList<>());

    public void addPoint(Point point) {
        points.add(0, point);
    }

    public List<Point> getPoints() {
        return points;
    }
}
