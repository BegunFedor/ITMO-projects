<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="javaweb.points.Point" %>
<%@ page import="java.text.SimpleDateFormat" %>

<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Точка в области • Вариант 471733</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>

<!-- МОДАЛЬНОЕ ОКНО -->
<div id="myModal" class="modal">
  <div class="modal-content">
    <span class="close" id="modalCloseBtn">&times;</span>
    <p id="modalMessage">Сообщение по умолчанию</p>
    <button id="modalOkBtn">OK</button>
  </div>
</div>

<form method="GET" action="controller" id="mainForm">
  <table class="main-container">
    <tr>
      <td colspan="2">
        <div class="header">
          Бегун Фёдор, P3219, Вариант 471733
        </div>
      </td>
    </tr>

    <!-- сохраняем последнюю точку -->
    <input type="hidden" id="lastPointX" value="<%= session.getAttribute("point") != null ? ((Point)session.getAttribute("point")).getX() : "" %>">
    <input type="hidden" id="lastPointY" value="<%= session.getAttribute("point") != null ? ((Point)session.getAttribute("point")).getY() : "" %>">
    <input type="hidden" id="lastPointR" value="<%= session.getAttribute("point") != null ? ((Point)session.getAttribute("point")).getR() : "" %>">
    <input type="hidden" id="lastPointHit" value="<%= session.getAttribute("point") != null ? ((Point)session.getAttribute("point")).isHit() : "" %>">

    <tr>
      <td class="form-section">
        <table class="form-table">
          <tr>
            <td colspan="2" class="form-title">Введите параметры:</td>
          </tr>

          <!-- X -->
          <tr>
            <td class="label-cell"><label>X координата:</label></td>
            <td class="input-cell">
              <div class="button-group">
                <button type="button" class="x-btn" value="-2">−2</button>
                <button type="button" class="x-btn" value="-1.5">−1.5</button>
                <button type="button" class="x-btn" value="-1">−1</button>
                <button type="button" class="x-btn" value="-0.5">−0.5</button>
                <button type="button" class="x-btn" value="0">0</button>
                <button type="button" class="x-btn" value="0.5">0.5</button>
                <button type="button" class="x-btn" value="1">1</button>
                <button type="button" class="x-btn" value="1.5">1.5</button>
                <button type="button" class="x-btn" value="2">2</button>
              </div>
              <input type="hidden" id="x-value" name="x" value="">
            </td>
          </tr>

          <!-- Y -->
          <tr>
            <td class="label-cell"><label for="y-input">Y координата:</label></td>
            <td class="input-cell">
              <input type="text" id="y-input" name="y" placeholder="от −3 до 3">
            </td>
          </tr>

          <!-- R -->
          <tr>
            <td class="label-cell"><label>Радиус R:</label></td>
            <td class="input-cell">
              <div class="checkbox-group">
                <label><input type="checkbox" name="r" value="1">1</label>
                <label><input type="checkbox" name="r" value="1.5">1.5</label>
                <label><input type="checkbox" name="r" value="2">2</label>
                <label><input type="checkbox" name="r" value="2.5">2.5</label>
                <label><input type="checkbox" name="r" value="3">3</label>
              </div>
              <input type="hidden" id="r-value" name="r" value="">
            </td>
          </tr>

          <tr>
            <td colspan="2" class="button-cell">
              <button type="submit" class="submit-button" id="submit-button">
                <span class="button-text">Проверить попадание</span>
                <span class="button-icon">→</span>
              </button>
            </td>
          </tr>
        </table>
      </td>

      <!-- ГРАФИК -->
      <td class="image-section">
        <div class="graph-title">Область попадания:</div>
        <div class="graph-container">
          <canvas id="graph-canvas" width="400" height="400"></canvas>
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
            </tr>
            </thead>
            <tbody id="results-body">
            <%
              List<Point> points = (List<Point>) session.getAttribute("allPoints");
              if (points == null) {
            %>
            <tr><td colspan="6" class="no-data">Нет данных</td></tr>
            <%
            } else {
              SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
              for (Point p : points) {
            %>
            <tr>
              <td><%= p.getX() %></td>
              <td><%= p.getY() %></td>
              <td><%= p.getR() %></td>
              <td><%= p.isHit() ? "Попал" : "Промазал" %></td>
              <td><%= fmt.format(p.getTimestamp()) %></td>
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
</form>

<script src="script1.js"></script>
</body>
</html>