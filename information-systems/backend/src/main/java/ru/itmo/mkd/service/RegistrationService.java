package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.auth.RegistrationRequest;
import ru.itmo.mkd.dto.auth.UserDto;
import ru.itmo.mkd.exception.NotFoundException;
import ru.itmo.mkd.mapper.UserMapper;
import ru.itmo.mkd.procedure.AccountProcedures;
import ru.itmo.mkd.repository.AppUserRepository;
import ru.itmo.mkd.security.PasswordHasher;

@Service
public class RegistrationService {

    private final AccountProcedures accountProcedures;
    private final AppUserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;

    public RegistrationService(AccountProcedures accountProcedures, AppUserRepository userRepository,
                               PasswordHasher passwordHasher, UserMapper userMapper) {
        this.accountProcedures = accountProcedures;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserDto register(RegistrationRequest request) {
        Long id = accountProcedures.registerResident(request.login().trim(),
                passwordHasher.hash(request.password()), request.fullName().trim(),
                blankToNull(request.phone()), blankToNull(request.email()),
                request.apartmentId(), request.ownership());
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> NotFoundException.of("User " + id));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
