package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.announcement.AnnouncementDto;
import ru.itmo.mkd.dto.announcement.PublishAnnouncementDto;
import ru.itmo.mkd.mapper.AnnouncementMapper;
import ru.itmo.mkd.procedure.AnnouncementProcedures;
import ru.itmo.mkd.repository.AnnouncementRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.StaffScope;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementProcedures announcementProcedures;
    private final StaffScope staffScope;
    private final AnnouncementMapper mapper;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               AnnouncementProcedures announcementProcedures, StaffScope staffScope,
                               AnnouncementMapper mapper) {
        this.announcementRepository = announcementRepository;
        this.announcementProcedures = announcementProcedures;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> residentFeed(CurrentUser resident) {
        return mapper.toFeedDtos(announcementRepository.findVisibleForResident(resident.id(), OffsetDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> companyAnnouncements(CurrentUser admin) {
        return mapper.toDtos(announcementRepository.findByCompany(staffScope.companyOf(admin)));
    }

    @Transactional
    public Long publish(CurrentUser admin, PublishAnnouncementDto dto) {
        return announcementProcedures.publish(admin.id(), dto.title().trim(), dto.body().trim(), dto.houseIds(),
                dto.visibleFrom(), dto.visibleTo());
    }
}
