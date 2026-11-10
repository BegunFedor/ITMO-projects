package ru.itmo.mkd.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.itmo.mkd.dto.announcement.AnnouncementDto;
import ru.itmo.mkd.entity.Announcement;

import java.util.List;

@Mapper(uses = ReferenceMapper.class)
public interface AnnouncementMapper {

    AnnouncementDto toDto(Announcement announcement);

    List<AnnouncementDto> toDtos(List<Announcement> announcements);

    @Named("feed")
    @Mapping(target = "houses", ignore = true)
    AnnouncementDto toFeedDto(Announcement announcement);

    @IterableMapping(qualifiedByName = "feed")
    List<AnnouncementDto> toFeedDtos(List<Announcement> announcements);
}
