package ru.itmo.mkd.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.domain.UserRole;
import ru.itmo.mkd.dto.reference.ConfirmResidentRequest;
import ru.itmo.mkd.dto.reference.PendingResidentDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.ResidentConfirmationService;

import java.util.List;

@RestController
@RequestMapping("/api/company/residents")
@RequiresRole(UserRole.COMPANY_ADMIN)
public class ResidentConfirmationController {

    private final ResidentConfirmationService confirmationService;

    public ResidentConfirmationController(ResidentConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @GetMapping("/pending")
    public List<PendingResidentDto> pending(CurrentUser user) {
        return confirmationService.pending(user);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirm(CurrentUser user, @Valid @RequestBody ConfirmResidentRequest request) {
        confirmationService.confirm(user, request);
    }
}
