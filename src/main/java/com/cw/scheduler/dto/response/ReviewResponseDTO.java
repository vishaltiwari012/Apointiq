package com.cw.scheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private String userName;
    private String serviceName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
