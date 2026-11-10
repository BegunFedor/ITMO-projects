package ru.itmo.mkd.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.dto.auth.LoginRequest;
import ru.itmo.mkd.dto.auth.RegistrationRequest;
import ru.itmo.mkd.dto.auth.UserDto;
import ru.itmo.mkd.security.Authenticated;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.SessionAuthentication;
import ru.itmo.mkd.service.AuthService;
import ru.itmo.mkd.service.RegistrationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final SessionAuthentication sessionAuthentication;

    public AuthController(AuthService authService, RegistrationService registrationService,
                          SessionAuthentication sessionAuthentication) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.sessionAuthentication = sessionAuthentication;
    }

    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest login, HttpServletRequest http) {
        UserDto user = authService.authenticate(login);
        sessionAuthentication.signIn(http, new CurrentUser(user.id(), user.role()));
        return user;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegistrationRequest request, HttpServletRequest http) {
        UserDto user = registrationService.register(request);
        sessionAuthentication.signIn(http, new CurrentUser(user.id(), user.role()));
        return user;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest http) {
        sessionAuthentication.signOut(http);
    }

    @GetMapping("/me")
    @Authenticated
    public UserDto me(CurrentUser user) {
        return authService.profile(user);
    }
}
