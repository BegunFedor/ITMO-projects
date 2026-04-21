package org.example.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import org.example.dao.AreaRepository;
import org.example.dao.PointRepository;
import org.example.dto.CheckRequest;
import org.example.entity.Area;
import org.example.entity.Point;
import org.example.entity.User;
import org.example.exception.ValidationException;
import org.example.tools.ValidationAnswer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Stateless
public class AreaCheckService {
    private final Set<Double> validRValues = Set.of(1.0, 2.0, 3.0, 4.0);

    @EJB
    private PointRepository pointRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @EJB
    private AreaRepository areaRepository;

    public Point checkPoint(CheckRequest request, User user) {
        long startTime = System.nanoTime();
        ValidationAnswer answer = validate(request);
        if (!answer.status()) {
            throw ValidationException.withDescription(
                    answer.parameter(),
                    answer.message()
            );
        }

        BigDecimal x = request.getX();
        BigDecimal y = request.getY();
        Float rF = request.getR();

        boolean hit;
        if (request.getAreaId() == null) {
            hit = checkHitDefault(x, y, rF);
        } else {
            Area area = areaRepository.findById(request.getAreaId())
                    .orElseThrow(() ->
                            new RuntimeException("Область с id " + request.getAreaId() + " не найдена")
                    );

            hit = checkHitForArea(x, y, rF, area);
        }

        Point point = Point.builder()
                .x(x)
                .y(y)
                .r(rF)
                .hit(hit)
                .currentTime(LocalDateTime.now())
                .executionTime((System.nanoTime() - startTime) / 1000)
                .user(user)
                .build();

        Point savedPoint = pointRepository.save(point);


        pointRepository.updateCoords(
                savedPoint.getId(),
                x.doubleValue(),
                y.doubleValue()
        );

        return savedPoint;
    }


    private ValidationAnswer validate(CheckRequest params) {
        try {
            if (params.getR() == null) return new ValidationAnswer(false, "r", "отсутствует");
            if (params.getX() == null) return new ValidationAnswer(false, "x", "отсутствует");
            if (params.getY() == null) return new ValidationAnswer(false, "y", "отсутствует");

            boolean validR = false;
            for (Double num : validRValues) {
                if (num.equals(params.getR().doubleValue())) {
                    validR = true;
                    break;
                }
            }
            if (!validR) {
                return new ValidationAnswer(false, "r", "должен быть из: " + validRValues);
            }
            if (params.getR() < 0) {
                return new ValidationAnswer(false, "r", "не может быть отрицательным");
            }

            return new ValidationAnswer(true, null, null);

        } catch (Exception ex) {
            return new ValidationAnswer(false, "error", "Ошибка валидации: " + ex.getMessage());
        }
    }


    private boolean checkHitDefault(BigDecimal x, BigDecimal y, Float rF) {
        if (rF == 0) return false;

        BigDecimal r = BigDecimal.valueOf(rF);
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal rHalf = r.multiply(BigDecimal.valueOf(0.5));

        boolean inQuarterCircle = false;
        if (x.compareTo(zero) >= 0 && y.compareTo(zero) >= 0) {
            BigDecimal distSq = x.pow(2).add(y.pow(2));
            BigDecimal rSq = r.pow(2);
            inQuarterCircle = distSq.compareTo(rSq) <= 0;
        }

        boolean inSquare =
                x.compareTo(zero) >= 0 &&
                        x.compareTo(r) <= 0 &&
                        y.compareTo(r.negate()) >= 0 &&
                        y.compareTo(zero) <= 0;

        boolean inTriangle = false;
        if (x.compareTo(zero) <= 0 && y.compareTo(zero) <= 0) {
            BigDecimal minusRHalf = rHalf.negate();
            if (x.compareTo(minusRHalf) >= 0 &&
                    y.compareTo(minusRHalf) >= 0) {

                BigDecimal sum = x.add(y);
                inTriangle = sum.compareTo(minusRHalf) >= 0;
            }
        }

        return inQuarterCircle || inSquare || inTriangle;
    }


    private boolean checkHitForArea(BigDecimal x, BigDecimal y, Float rF, Area area) {
        if (area == null || area.getSchemaJson() == null) {
            return checkHitDefault(x, y, rF);
        }

        try {
            JsonNode root = objectMapper.readTree(area.getSchemaJson());
            double xd = x.doubleValue();
            double yd = y.doubleValue();
            return isPointInNode(root, xd, yd);
        } catch (Exception e) {
            e.printStackTrace();
            return checkHitDefault(x, y, rF);
        }
    }


    private boolean isPointInNode(JsonNode node, double x, double y) {
        if (node == null || node.isNull()) return false;

        if (node.has("shape")) {
            String shape = node.get("shape").asText();
            switch (shape) {
                case "circle":
                    return isInsideCircle(node, x, y);
                case "rectangle":
                    return isInsideRectangle(node, x, y);
                case "polygon":
                    return isInsidePolygon(node, x, y);
                case "halfPlane":
                    return isInsideHalfPlane(node, x, y);
                default:
                    return false;
            }
        }

        if (node.has("operation")) {
            String op = node.get("operation").asText();
            JsonNode operands = node.get("operands");

            if (operands == null || !operands.isArray() || operands.size() == 0) {
                return false;
            }

            switch (op) {
                case "or": {
                    for (JsonNode child : operands) {
                        if (isPointInNode(child, x, y)) return true;
                    }
                    return false;
                }
                case "and": {
                    for (JsonNode child : operands) {
                        if (!isPointInNode(child, x, y)) return false;
                    }
                    return true;
                }
                case "not": {
                    JsonNode child = operands.get(0);
                    return !isPointInNode(child, x, y);
                }
                default:
                    return false;
            }
        }

        return false;
    }

    private boolean isInsideCircle(JsonNode node, double x, double y) {
        JsonNode centerNode = node.path("center");
        double cx = centerNode.path(0).asDouble();
        double cy = centerNode.path(1).asDouble();
        double r = node.path("radius").asDouble();

        double dx = x - cx;
        double dy = y - cy;
        return dx * dx + dy * dy <= r * r + 1e-9;
    }

    private boolean isInsideRectangle(JsonNode node, double x, double y) {
        double x1 = node.path("x1").asDouble();
        double x2 = node.path("x2").asDouble();
        double y1 = node.path("y1").asDouble();
        double y2 = node.path("y2").asDouble();

        double xmin = Math.min(x1, x2);
        double xmax = Math.max(x1, x2);
        double ymin = Math.min(y1, y2);
        double ymax = Math.max(y1, y2);

        double eps = 1e-9;
        return x >= xmin - eps && x <= xmax + eps
                && y >= ymin - eps && y <= ymax + eps;
    }

    private boolean isInsidePolygon(JsonNode node, double x, double y) {
        JsonNode verts = node.path("vertices");
        if (!verts.isArray() || verts.size() < 3) return false;

        boolean inside = false;
        int n = verts.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = verts.get(i).get(0).asDouble();
            double yi = verts.get(i).get(1).asDouble();
            double xj = verts.get(j).get(0).asDouble();
            double yj = verts.get(j).get(1).asDouble();

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / ((yj - yi) + 1e-12) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    private boolean isInsideHalfPlane(JsonNode node, double x, double y) {
        double a = node.path("a").asDouble();
        double b = node.path("b").asDouble();
        double c = node.path("c").asDouble();
        String sign = node.path("sign").asText("ge");

        double val = a * x + b * y + c;
        double eps = 1e-9;

        if ("le".equals(sign)) {
            return val <= eps;
        } else {
            return val >= -eps;
        }
    }
}