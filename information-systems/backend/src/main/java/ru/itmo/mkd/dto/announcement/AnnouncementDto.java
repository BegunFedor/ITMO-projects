package ru.itmo.mkd.dto.announcement;

import ru.itmo.mkd.dto.reference.HouseDto;

import java.time.OffsetDateTime;
import java.util.List;

public record AnnouncementDto(Long id, String title, String body, OffsetDateTime visibleFrom,
                              OffsetDateTime visibleTo, List<HouseDto> houses) {
}
