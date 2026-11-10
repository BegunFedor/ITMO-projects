package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.itmo.mkd.dto.supervision.ComplaintDto;
import ru.itmo.mkd.dto.supervision.OrderDto;
import ru.itmo.mkd.dto.supervision.OverdueRequestDto;
import ru.itmo.mkd.entity.Complaint;
import ru.itmo.mkd.entity.InspectionOrder;
import ru.itmo.mkd.entity.Request;
import ru.itmo.mkd.procedure.OverdueRequestRow;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper
public interface SupervisionMapper {

    @Mapping(target = "companyName", source = "company")
    @Mapping(target = "categoryName", source = "category")
    OverdueRequestDto toDto(OverdueRequestRow row);

    List<OverdueRequestDto> toOverdueDtos(List<OverdueRequestRow> rows);

    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "residentName", source = "resident.fullName")
    @Mapping(target = "address", source = "request.house.address")
    @Mapping(target = "categoryName", source = "request.category.name")
    @Mapping(target = "companyId", source = "request.house.company.id")
    @Mapping(target = "companyName", source = "request.house.company.name")
    ComplaintDto toDto(Complaint complaint);

    List<ComplaintDto> toComplaintDtos(List<Complaint> complaints);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "requestIds", source = "requests", qualifiedByName = "requestIds")
    OrderDto toDto(InspectionOrder order);

    List<OrderDto> toOrderDtos(List<InspectionOrder> orders);

    @Named("requestIds")
    default List<Long> requestIds(Set<Request> requests) {
        return requests.stream().map(Request::getId).sorted(Comparator.naturalOrder()).toList();
    }
}
