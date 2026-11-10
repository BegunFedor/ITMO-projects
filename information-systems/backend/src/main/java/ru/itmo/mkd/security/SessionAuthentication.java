package ru.itmo.mkd.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SessionAuthentication {

    private static final String ATTRIBUTE = SessionAuthentication.class.getName() + ".user";

    public Optional<CurrentUser> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((CurrentUser) session.getAttribute(ATTRIBUTE));
    }

    public void signIn(HttpServletRequest request, CurrentUser user) {
        request.getSession(true);
        request.changeSessionId();
        request.getSession().setAttribute(ATTRIBUTE, user);
    }

    public void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
