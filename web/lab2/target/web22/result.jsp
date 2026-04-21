<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="javaweb.points.Point" %>
<%@ page import="javaweb.points.PointBean" %>
<%@ page import="java.text.SimpleDateFormat" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Точка в области</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>


<table class="main-container">
    <tr>
        <td colspan="2">
            <div class="header">
                <a href="index.jsp">⟵</a>
                Бегун Фёдор, P3219, ИСУ 471733
            </div>
        </td>
    </tr>

    <input type="hidden" id="lastPointX"
           value="<%= ((Point)request.getAttribute("point")).getX() %>">
    <input type="hidden" id="lastPointY"
           value="<%= ((Point)request.getAttribute("point")).getY() %>">
    <input type="hidden" id="lastPointR"
           value="<%= ((Point)request.getAttribute("point")).getR() %>">
    <input type="hidden" id="lastPointHit"
           value="<%= ((Point)request.getAttribute("point")).isHit() %>">
    <tr class="main">
        <td class="image-section">
            <div class="graph-title">Область попадания:</div>
            <div class="graph-container">
                <canvas id="graph-canvas1" width="400" height="400" ></canvas>
                <div class="point-info">
                    <h3>Информация о точке</h3>
                    <%
                        Point lastPoint = null;
                        List<Point> points = (List<Point>) application.getAttribute("allPoints");
                        if (points != null && !points.isEmpty()) {
                            lastPoint = points.get(0);
                        }
                    %>
                    <% if (lastPoint != null)
                    {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    %>
                    <div><strong>X:</strong> <%= lastPoint.getX() %></div>
                    <div><strong>Y:</strong> <%= lastPoint.getY() %></div>
                    <div><strong>R:</strong> <%= lastPoint.getR() %></div>
                    <div><strong>Результат:</strong> <%= lastPoint.isHit() ? "Попал" : "Промазал" %></div>
                    <div><strong>Время:</strong> <%= timeFormat.format(lastPoint.getTimestamp()) %></div>
                    <div><strong>Время работы (мс):</strong> <%= String.format("%.3f", lastPoint.getExecutionTime() / 1000000.0) %></div><% } else { %>
                    <div><strong>Таймзона:</strong> <%= lastPoint.getTimezone() != null ? lastPoint.getTimezone() : "—" %></div>
                    <div>Нет данных о точках</div>
                    <% } %>
                </div>
            </div>

        </td>


    </tr>

    <tr>
        <td colspan="2">
            <div class="rtd">
                <table class="results-table">
                    <thead>
                    <tr>
                        <th>X</th>
                        <th>Y</th>
                        <th>R</th>
                        <th>Результат</th>
                        <th>Время</th>
                        <th>Время работы (мс)</th>
                        <th>Таймзона</th>
                    </tr>
                    </thead>
                    <tbody id="results-body">
                    <%
                        if (points == null){
                    %>
                    <tr>
                        <td colspan="6" class="no-data">Нет данных</td>
                    </tr>
                    <%
                    }else{
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                    %>
                    <tr>
                        <td><%= lastPoint.getX() %></td>
                        <td><%= lastPoint.getY() %></td>
                        <td><%= lastPoint.getR() %></td>
                        <td><%= lastPoint.isHit() ? "Попал" : "Промазал" %></td>
                        <td><%= timeFormat.format(lastPoint.getTimestamp()) %></td>
                        <td><%= String.format("%.3f", lastPoint.getExecutionTime() / 1000000.0) %></td>
                        <td><%= lastPoint.getTimezone() != null ? lastPoint.getTimezone() : "—" %></td>
                    </tr>
                    <%

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