package org.example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AreaDto {
    private Long id;
    private String name;
    private String createdAt;
    private Object schema;
}
