package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckRequest {

    private BigDecimal x;
    private BigDecimal y;
    private Float r;
    private Long areaId;
}