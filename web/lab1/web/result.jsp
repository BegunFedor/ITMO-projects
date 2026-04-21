<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="javaweb.points.Point" %>
<%@ page import="java.text.SimpleDateFormat" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Результаты проверки</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>

<table class="main-container">
    <tr>
        <td colspan="2">
            <div class="header">
                <a href="index.jsp">⟵</a> Бегун Фёдор, P3219, Вариант 471733
            </div>
        </td>
    </tr>

    <%
        Point lastPoint = (Point) session.getAttribute("point");
        List<Point> points = (List<Point>) session.getAttribute("allPoints");
        if (lastPoint != null) {
            SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
    %>
    <input type="hidden" id="lastPointX" value="<%= lastPoint.getX() %>">
    <input type="hidden" id="lastPointY" value="<%= lastPoint.getY() %>">
    <input type="hidden" id="lastPointR" value="<%= lastPoint.getR() %>">
    <input type="hidden" id="lastPointHit" value="<%= lastPoint.isHit() %>">

    <tr class="main">
        <td class="image-section">
            <div class="graph-title">Область попадания:</div>
            <div class="graph-container">
                <canvas id="graph-canvas1" width="400" height="400"></canvas>
                <div class="point-info">
                    <h3>Информация о последней точке</h3>
                    <div><strong>X:</strong> <%= lastPoint.getX() %></div>
                    <div><strong>Y:</strong> <%= lastPoint.getY() %></div>
                    <div><strong>R:</strong> <%= lastPoint.getR() %></div>
                    <div><strong>Результат:</strong> <%= lastPoint.isHit() ? "Попал" : "Промах" %></div>
                    <div><strong>Время:</strong> <%= fmt.format(lastPoint.getTimestamp()) %></div>
                    <div><strong>Время работы (мс):</strong> <%= String.format("%.3f", lastPoint.getExecutionTime() / 1_000_000.0) %></div>
                </div>
            </div>
        </td>
    </tr>
    <%
    } else {
    %>
    <tr><td><p style="text-align:center; color:#9d174d;">Нет данных о точках.</p></td></tr>
    <%
        }
    %>

    <tr>
        <td colspan="2">
            <div class="rtd">
                <table class="results-table">
                    <thead>
                    <tr>
                        <th>X</th><th>Y</th><th>R</th>
                        <th>Результат</th><th>Время</th><th>Время работы (мс)</th>
                    </tr>
                    </thead>
                    <tbody id="results-body">
                    <%
                        if (points == null) {
                    %>
                    <tr><td colspan="6" class="no-data">Нет данных</td></tr>
                    <%
                    } else {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        for (Point p : points) {
                    %>
                    <tr>
                        <td><%= p.getX() %></td>
                        <td><%= p.getY() %></td>
                        <td><%= p.getR() %></td>
                        <td><%= p.isHit() ? "Попал" : "Промазал" %></td>
                        <td><%= timeFormat.format(p.getTimestamp()) %></td>
                        <td><%= String.format("%.3f", p.getExecutionTime() / 1_000_000.0) %></td>
                    </tr>
                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
            </div>
        </td>
    </tr>
</table>

<script src="scriptres.js"></script>
</body>
</html>