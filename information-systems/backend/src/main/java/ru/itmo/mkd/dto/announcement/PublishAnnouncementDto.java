package ru.itmo.mkd.dto.announcement;

import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.util.List;

public record PublishAnnouncementDto(@NotBlank @Size(max = 200) String title,
                                     @NotBlank @Size(max = 5000) String body,
                                     @NotEmpty List<@NotNull Long> houseIds,
                                     OffsetDateTime visibleFrom,
                                     OffsetDateTime visibleTo) {
}
