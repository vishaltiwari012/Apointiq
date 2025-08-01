package com.cw.scheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryWithServiceCountDTO {
    private Long id;
    private String name;
    private Long serviceCount;
}
