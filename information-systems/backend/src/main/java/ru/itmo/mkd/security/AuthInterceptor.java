package ru.itmo.mkd.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.itmo.mkd.exception.AuthenticationException;
import ru.itmo.mkd.exception.ErrorCodes;
import ru.itmo.mkd.exception.ForbiddenException;

import java.util.Arrays;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    static final String CSRF_HEADER = "X-Requested-With";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final SessionAuthentication sessionAuthentication;

    public AuthInterceptor(SessionAuthentication sessionAuthentication) {
        this.sessionAuthentication = sessionAuthentication;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!SAFE_METHODS.contains(request.getMethod()) && request.getHeader(CSRF_HEADER) == null) {
            throw new ForbiddenException(ErrorCodes.CSRF_HEADER_MISSING, "Missing " + CSRF_HEADER + " header");
        }
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequiresRole requiresRole = findAnnotation(method, RequiresRole.class);
        boolean authenticated = requiresRole != null || findAnnotation(method, Authenticated.class) != null;
        if (!authenticated) {
            return true;
        }
        CurrentUser user = sessionAuthentication.current(request)
                .orElseThrow(() -> AuthenticationException.of("Not signed in"));
        if (requiresRole != null && Arrays.stream(requiresRole.value()).noneMatch(r -> r == user.role())) {
            throw ForbiddenException.of("Role " + user.role() + " is not allowed");
        }
        return true;
    }

    private static <A extends java.lang.annotation.Annotation> A findAnnotation(HandlerMethod method, Class<A> type) {
        A onMethod = AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), type);
        return onMethod != null ? onMethod : AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), type);
    }

}
