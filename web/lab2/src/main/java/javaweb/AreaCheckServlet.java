package javaweb;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.inject.Inject;
import javaweb.points.Point;
import javaweb.points.PointBean;
import javaweb.validation.InputValidator;

import java.io.IOException;

@WebServlet("/area")
public class AreaCheckServlet extends HttpServlet {

    @Inject
    private PointBean pointBean;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getDispatcherType() != DispatcherType.FORWARD) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Прямой доступ к /area запрещён.");
            return;
        }
        String timezone = request.getParameter("timezone");
        long startTime = System.nanoTime();

        String xStr = request.getParameter("x");
        String yStr = request.getParameter("y");
        String rStr = request.getParameter("r");

        if (!InputValidator.canParse(xStr, yStr, rStr)) {
            forwardWithError(request, response, "Некорректный формат данных!");
            return;
        }

        double x = Double.parseDouble(xStr);
        double y = Double.parseDouble(yStr);
        double r = Double.parseDouble(rStr);

        if (!InputValidator.isValid(x, y, r)) {
            forwardWithError(request, response, "Некорректные данные! Попробуйте снова.");
            return;
        }

        Point point = new Point(x, y, r);
        long executionTime = System.nanoTime() - startTime;
        point.setExecutionTime(executionTime);

        point.setTimezone(timezone);

        pointBean.addPoint(point);
        request.setAttribute("point", point);
        request.setAttribute("timezone", timezone);
        request.setAttribute("executionTime", executionTime / 1_000_000.0);
        getServletContext().setAttribute("allPoints", pointBean.getPoints());

        try {
            request.getRequestDispatcher("result.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException("Ошибка при переходе на result.jsp", e);
        }
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        try {
            request.setAttribute("errorMessage", message);
            request.getRequestDispatcher("index.jsp").forward(request, response);
        } catch (Exception e) {
            throw new IOException("Ошибка при переходе на index.jsp", e);
        }
    }
}