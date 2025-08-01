package com.cw.scheduler.service.impl;

import com.cw.scheduler.entity.Role;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.RoleRepository;
import com.cw.scheduler.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getByName(String name) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.warn("Requested role not found: {}", name);
                    return new ResourceNotFoundException("Role " + name + " not found");
                });
    }

    @Override
    public Role getCustomerRole() {
        return getByName("CUSTOMER");
    }

    @Override
    public Role getAdminRole() {
        return getByName("ADMIN");
    }

    @Override
    public Role getServiceProviderRole() {
        return getByName("SERVICE_PROVIDER");
    }

}
