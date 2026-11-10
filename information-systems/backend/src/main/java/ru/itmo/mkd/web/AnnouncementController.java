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
import ru.itmo.mkd.dto.CreatedDto;
import ru.itmo.mkd.dto.announcement.AnnouncementDto;
import ru.itmo.mkd.dto.announcement.PublishAnnouncementDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/resident/announcements")
    @RequiresRole(UserRole.RESIDENT)
    public List<AnnouncementDto> feed(CurrentUser user) {
        return announcementService.residentFeed(user);
    }

    @GetMapping("/company/announcements")
    @RequiresRole(UserRole.COMPANY_ADMIN)
    public List<AnnouncementDto> company(CurrentUser user) {
        return announcementService.companyAnnouncements(user);
    }

    @PostMapping("/company/announcements")
    @RequiresRole(UserRole.COMPANY_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto publish(CurrentUser user, @Valid @RequestBody PublishAnnouncementDto dto) {
        return new CreatedDto(announcementService.publish(user, dto));
    }
}
