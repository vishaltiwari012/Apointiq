package com.cw.scheduler.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequestDTO {
    private String name;
    private Set<String> authorityNames;
}