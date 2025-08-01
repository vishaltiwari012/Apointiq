package com.cw.scheduler.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderRejectionRequestDTO {
    private Long userId;
    private String rejectionReason;
}
