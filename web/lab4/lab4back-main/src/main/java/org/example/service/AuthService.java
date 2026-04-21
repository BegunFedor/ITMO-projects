package org.example.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.example.dao.UserRepository;
import org.example.dto.AuthResponse;
import org.example.dto.RegisterResponse;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.exception.NameAlreadyExistsException;
import org.example.exception.InvalidPassword;
import org.example.exception.UserNotFoundException;
import org.example.tools.PasswordEncoder;

@Stateless
public class AuthService {

    @EJB
    private UserRepository userRepository;

    @EJB
    private RefreshTokenService refreshTokenService;

    @EJB
    private PasswordEncoder passwordEncoder;

    @EJB
    private JWTService jwtService;

    public AuthResponse authUser(String email, String password) {
        User user = userRepository.findByName(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с такой почтой не найден"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPassword(email);
        }


        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(15 * 60L)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public RegisterResponse registerUser(String email, String password, String roleStr) {
        if (userRepository.findByName(email).isPresent()) {
            throw new NameAlreadyExistsException(email);
        }
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            role = Role.USER;
        }

        User user = User.builder()
                .name(email)
                .password(passwordEncoder.encode(password))
                .verified(true)
                .role(role)
                .build();

        userRepository.save(user);

        return RegisterResponse.builder()
                .email(email)
                .message("Регистрация успешно завершена")
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
}