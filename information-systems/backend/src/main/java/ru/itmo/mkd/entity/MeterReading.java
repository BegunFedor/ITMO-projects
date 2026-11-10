package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "meter_reading")
public class MeterReading {

    @Id
    @Column(name = "reading_id")
    private Long id;

    @Column(name = "meter_id")
    private Long meterId;

    @Column(name = "reading_date")
    private LocalDate readingDate;

    private BigDecimal value;

    protected MeterReading() {
    }

    public Long getId() {
        return id;
    }

    public Long getMeterId() {
        return meterId;
    }

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public BigDecimal getValue() {
        return value;
    }
}
