package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.supervision.IssueOrderDto;
import ru.itmo.mkd.dto.supervision.OrderDto;
import ru.itmo.mkd.dto.supervision.OverdueRequestDto;
import ru.itmo.mkd.dto.supervision.ReviewOrderDto;
import ru.itmo.mkd.mapper.SupervisionMapper;
import ru.itmo.mkd.procedure.InspectionProcedures;
import ru.itmo.mkd.repository.InspectionOrderRepository;
import ru.itmo.mkd.security.CurrentUser;

import java.util.List;

@Service
public class InspectionService {

    private final InspectionProcedures inspectionProcedures;
    private final InspectionOrderRepository orderRepository;
    private final SupervisionMapper mapper;

    public InspectionService(InspectionProcedures inspectionProcedures, InspectionOrderRepository orderRepository,
                             SupervisionMapper mapper) {
        this.inspectionProcedures = inspectionProcedures;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<OverdueRequestDto> overdueRequests(CurrentUser inspector) {
        return mapper.toOverdueDtos(inspectionProcedures.overdueRequests(inspector.id()));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> orders(CurrentUser inspector) {
        return mapper.toOrderDtos(orderRepository.findByInspector(inspector.id()));
    }

    @Transactional
    public Long issue(CurrentUser inspector, IssueOrderDto dto) {
        return inspectionProcedures.issueOrder(inspector.id(), dto.companyId(), dto.violation().trim(),
                dto.dueDate(), nullToEmpty(dto.requestIds()), nullToEmpty(dto.complaintIds()));
    }

    @Transactional
    public void review(CurrentUser inspector, Long orderId, ReviewOrderDto dto) {
        String comment = dto.comment() == null || dto.comment().isBlank() ? null : dto.comment().trim();
        inspectionProcedures.reviewOrder(orderId, inspector.id(), dto.accepted(), comment);
    }

    private static List<Long> nullToEmpty(List<Long> ids) {
        return ids == null ? List.of() : ids;
    }
}
