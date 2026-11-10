package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.meter.MeterDto;
import ru.itmo.mkd.entity.Meter;
import ru.itmo.mkd.entity.MeterReading;
import ru.itmo.mkd.mapper.MeterMapper;
import ru.itmo.mkd.procedure.MeterProcedures;
import ru.itmo.mkd.repository.MeterReadingRepository;
import ru.itmo.mkd.repository.MeterRepository;
import ru.itmo.mkd.security.CurrentUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MeterService {

    private final MeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final MeterProcedures meterProcedures;
    private final MeterMapper mapper;

    public MeterService(MeterRepository meterRepository, MeterReadingRepository readingRepository,
                        MeterProcedures meterProcedures, MeterMapper mapper) {
        this.meterRepository = meterRepository;
        this.readingRepository = readingRepository;
        this.meterProcedures = meterProcedures;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<MeterDto> residentMeters(CurrentUser resident) {
        List<Meter> meters = meterRepository.findActiveForResident(resident.id());
        if (meters.isEmpty()) {
            return List.of();
        }
        Map<Long, MeterReading> latest = readingRepository.findLatest(meters.stream().map(Meter::getId).toList())
                .stream()
                .collect(Collectors.toMap(MeterReading::getMeterId, Function.identity()));
        return meters.stream().map(m -> mapper.toDto(m, latest.get(m.getId()))).toList();
    }

    @Transactional
    public void submitReading(CurrentUser resident, Long meterId, BigDecimal value) {
        meterProcedures.submitReading(meterId, resident.id(), value);
    }
}
