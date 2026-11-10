package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.request.CreateRequestDto;
import ru.itmo.mkd.dto.request.RateDto;
import ru.itmo.mkd.procedure.RequestProcedures;
import ru.itmo.mkd.procedure.ResultProcedures;
import ru.itmo.mkd.security.CurrentUser;

@Service
public class ResidentRequestService {

    private final RequestProcedures requestProcedures;
    private final ResultProcedures resultProcedures;

    public ResidentRequestService(RequestProcedures requestProcedures, ResultProcedures resultProcedures) {
        this.requestProcedures = requestProcedures;
        this.resultProcedures = resultProcedures;
    }

    @Transactional
    public Long create(CurrentUser resident, CreateRequestDto dto) {
        return requestProcedures.createRequest(resident.id(), dto.houseId(), dto.apartmentId(), dto.categoryId(),
                dto.description().trim(), dto.previousRequestId());
    }

    @Transactional
    public void cancel(CurrentUser resident, Long requestId, String reason) {
        requestProcedures.cancelRequest(requestId, resident.id(), reason.trim());
    }

    @Transactional
    public void rate(CurrentUser resident, Long requestId, RateDto dto) {
        resultProcedures.rateRequest(requestId, resident.id(), dto.score(), trimToNull(dto.review()));
    }

    @Transactional
    public void rejectResult(CurrentUser resident, Long requestId, String reason) {
        resultProcedures.rejectResult(requestId, resident.id(), reason.trim());
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
