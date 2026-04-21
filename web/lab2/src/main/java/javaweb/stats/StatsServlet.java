package javaweb.stats;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/stats")
public class StatsServlet extends HttpServlet {

    @Inject
    private StatsBean statsBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> stats = statsBean.getStats();

        resp.setContentType("application/json; charset=UTF-8");

        StringBuilder json = new StringBuilder("{");
        json.append("\"totalRequests\":").append(stats.get("totalRequests")).append(",");
        json.append("\"passedPercent\":").append(stats.get("passedPercent")).append(",");
        json.append("\"blockedPercent\":").append(stats.get("blockedPercent")).append(",");
        json.append("\"timezonePercent\":").append(stats.get("timezonePercent").toString());
        json.append("}");

        resp.getWriter().write(json.toString());
    }
}
