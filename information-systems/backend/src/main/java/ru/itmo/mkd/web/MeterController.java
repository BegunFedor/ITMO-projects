package ru.itmo.mkd.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.domain.UserRole;
import ru.itmo.mkd.dto.meter.MeterDto;
import ru.itmo.mkd.dto.meter.ReadingDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.MeterService;

import java.util.List;

@RestController
@RequestMapping("/api/resident/meters")
@RequiresRole(UserRole.RESIDENT)
public class MeterController {

    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    @GetMapping
    public List<MeterDto> meters(CurrentUser user) {
        return meterService.residentMeters(user);
    }

    @PostMapping("/{id}/readings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReadingDto dto) {
        meterService.submitReading(user, id, dto.value());
    }
}
