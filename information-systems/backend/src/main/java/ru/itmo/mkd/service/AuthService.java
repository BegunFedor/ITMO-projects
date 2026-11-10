package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.auth.LoginRequest;
import ru.itmo.mkd.dto.auth.UserDto;
import ru.itmo.mkd.entity.AppUser;
import ru.itmo.mkd.exception.AuthenticationException;
import ru.itmo.mkd.exception.ErrorCodes;
import ru.itmo.mkd.exception.NotFoundException;
import ru.itmo.mkd.mapper.UserMapper;
import ru.itmo.mkd.repository.AppUserRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.PasswordHasher;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;

    public AuthService(AppUserRepository userRepository, PasswordHasher passwordHasher, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public UserDto authenticate(LoginRequest login) {
        AppUser user = userRepository.findByLoginIgnoreCase(login.login().trim())
                .filter(AppUser::isActive)
                .filter(u -> passwordHasher.matches(login.password(), u.getPasswordHash()))
                .orElseThrow(() -> new AuthenticationException(ErrorCodes.BAD_CREDENTIALS, "Bad credentials"));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto profile(CurrentUser current) {
        return userRepository.findById(current.id())
                .map(userMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("User " + current.id()));
    }
}
