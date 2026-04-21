package javaweb.filters;

import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javaweb.stats.StatsBean;

@WebFilter("/controller")
public class TimeZoneFilter implements Filter {

    @Inject
    private StatsBean statsBean;

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        String timezone = req.getHeader("X-Timezone");
        if (timezone == null || timezone.isEmpty()) {
            timezone = req.getParameter("timezone");
        }
        if (timezone == null || timezone.isEmpty()) {
            timezone = "Unknown";
        }

        statsBean.recordTimezone(timezone);
        if (timezone.equalsIgnoreCase("Europe/Berlin")) {
            statsBean.recordFilterResult(false);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Доступ с таймзоны " + timezone + " запрещён.");
            return;
        }

        statsBean.recordFilterResult(true);
        filterChain.doFilter(req, resp);
    }
}