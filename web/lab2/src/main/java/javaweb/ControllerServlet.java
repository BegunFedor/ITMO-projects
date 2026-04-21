package javaweb;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "GET-запросы к контроллеру запрещены.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse resp) {
        String xStr = request.getParameter("x");
        String yStr = request.getParameter("y");
        String rStr = request.getParameter("r");

        try {
            if (xStr != null && yStr != null && rStr != null) {
                request.getRequestDispatcher("/area").forward(request, resp);
            } else {
                request.getRequestDispatcher("/index.jsp").forward(request, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
