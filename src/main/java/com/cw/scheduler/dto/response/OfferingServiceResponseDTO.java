package com.cw.scheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferingServiceResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
}