package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.supervision.OrderDto;
import ru.itmo.mkd.mapper.SupervisionMapper;
import ru.itmo.mkd.procedure.InspectionProcedures;
import ru.itmo.mkd.repository.InspectionOrderRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.StaffScope;

import java.util.List;

@Service
public class OrderReportService {

    private final InspectionOrderRepository orderRepository;
    private final InspectionProcedures inspectionProcedures;
    private final StaffScope staffScope;
    private final SupervisionMapper mapper;

    public OrderReportService(InspectionOrderRepository orderRepository, InspectionProcedures inspectionProcedures,
                              StaffScope staffScope, SupervisionMapper mapper) {
        this.orderRepository = orderRepository;
        this.inspectionProcedures = inspectionProcedures;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> companyOrders(CurrentUser admin) {
        return mapper.toOrderDtos(orderRepository.findByCompany(staffScope.companyOf(admin)));
    }

    @Transactional
    public void submitReport(CurrentUser admin, Long orderId, String report) {
        inspectionProcedures.submitOrderReport(orderId, admin.id(), report.trim());
    }
}
