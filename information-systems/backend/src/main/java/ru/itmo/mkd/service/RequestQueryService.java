package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.domain.RequestStatus;
import ru.itmo.mkd.dto.request.RequestCardDto;
import ru.itmo.mkd.dto.request.RequestSummaryDto;
import ru.itmo.mkd.entity.Rating;
import ru.itmo.mkd.entity.Request;
import ru.itmo.mkd.entity.RequestStatusHistory;
import ru.itmo.mkd.exception.NotFoundException;
import ru.itmo.mkd.mapper.RequestMapper;
import ru.itmo.mkd.repository.ComplaintRepository;
import ru.itmo.mkd.repository.RatingRepository;
import ru.itmo.mkd.repository.RequestCommentRepository;
import ru.itmo.mkd.repository.RequestRepository;
import ru.itmo.mkd.repository.RequestStatusHistoryRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.RequestAccessPolicy;
import ru.itmo.mkd.service.access.RequestActionPolicy;
import ru.itmo.mkd.service.access.StaffScope;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class RequestQueryService {

    private static final Set<RequestStatus> EXECUTOR_STATUSES =
            EnumSet.of(RequestStatus.ASSIGNED, RequestStatus.IN_PROGRESS, RequestStatus.DONE);

    private final RequestRepository requestRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final RequestCommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final ComplaintRepository complaintRepository;
    private final RequestAccessPolicy accessPolicy;
    private final RequestActionPolicy actionPolicy;
    private final StaffScope staffScope;
    private final RequestMapper mapper;

    public RequestQueryService(RequestRepository requestRepository, RequestStatusHistoryRepository historyRepository,
                               RequestCommentRepository commentRepository, RatingRepository ratingRepository,
                               ComplaintRepository complaintRepository, RequestAccessPolicy accessPolicy,
                               RequestActionPolicy actionPolicy, StaffScope staffScope, RequestMapper mapper) {
        this.requestRepository = requestRepository;
        this.historyRepository = historyRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.complaintRepository = complaintRepository;
        this.accessPolicy = accessPolicy;
        this.actionPolicy = actionPolicy;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    public List<RequestSummaryDto> residentRequests(CurrentUser resident) {
        return mapper.toSummaries(requestRepository.findByAuthor(resident.id()));
    }

    public List<RequestSummaryDto> companyRequests(CurrentUser admin, Collection<RequestStatus> statuses) {
        Collection<RequestStatus> filter = statuses == null || statuses.isEmpty()
                ? EnumSet.allOf(RequestStatus.class) : statuses;
        return mapper.toSummaries(requestRepository.findByCompanyAndStatuses(staffScope.companyOf(admin), filter));
    }

    public List<RequestSummaryDto> executorTasks(CurrentUser employee) {
        return mapper.toSummaries(requestRepository.findByExecutorAndStatuses(employee.id(), EXECUTOR_STATUSES));
    }

    public RequestCardDto card(CurrentUser user, Long requestId) {
        Request request = load(requestId);
        accessPolicy.checkCanView(user, request);
        List<RequestStatusHistory> history = historyRepository.findByRequest(requestId);
        Rating rating = ratingRepository.findById(requestId).orElse(null);
        boolean hasComplaint = complaintRepository.existsByRequestId(requestId);
        return new RequestCardDto(
                mapper.toDetails(request),
                rating == null ? null : mapper.toDto(rating),
                hasComplaint,
                mapper.toHistoryDtos(history),
                mapper.toCommentDtos(commentRepository.findByRequest(requestId)),
                actionPolicy.actionsFor(user, request, rating, hasComplaint, history));
    }

    Request load(Long requestId) {
        return requestRepository.findCard(requestId)
                .orElseThrow(() -> NotFoundException.of("Request " + requestId));
    }
}
