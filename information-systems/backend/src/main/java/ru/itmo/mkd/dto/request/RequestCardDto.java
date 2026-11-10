package ru.itmo.mkd.dto.request;

import java.util.List;
import java.util.Set;

public record RequestCardDto(RequestDetailsDto request, RatingDto rating, boolean hasComplaint,
                             List<StatusHistoryDto> history, List<CommentDto> comments,
                             Set<RequestAction> actions) {
}
