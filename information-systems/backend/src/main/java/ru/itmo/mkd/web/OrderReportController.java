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
import ru.itmo.mkd.dto.supervision.OrderDto;
import ru.itmo.mkd.dto.supervision.OrderReportDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.OrderReportService;

import java.util.List;

@RestController
@RequestMapping("/api/company/orders")
@RequiresRole(UserRole.COMPANY_ADMIN)
public class OrderReportController {

    private final OrderReportService orderReportService;

    public OrderReportController(OrderReportService orderReportService) {
        this.orderReportService = orderReportService;
    }

    @GetMapping
    public List<OrderDto> orders(CurrentUser user) {
        return orderReportService.companyOrders(user);
    }

    @PostMapping("/{id}/report")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void report(CurrentUser user, @PathVariable Long id, @Valid @RequestBody OrderReportDto dto) {
        orderReportService.submitReport(user, id, dto.report());
    }
}
