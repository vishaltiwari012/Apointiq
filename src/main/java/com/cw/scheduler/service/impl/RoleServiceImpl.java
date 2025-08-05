package com.cw.scheduler.service.impl;

import com.cw.scheduler.entity.Role;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.RoleRepository;
import com.cw.scheduler.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Cacheable(value = "roles", key = "#name.toUpperCase()")
    public Role getByName(String name) {
        log.info("Fetching role by name: {}", name);

        Role role = roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    log.warn("Role not found in DB: {}", name);
                    return new ResourceNotFoundException("Role " + name + " not found");
                });

        log.debug("Role fetched from DB: id={}, name={}", role.getId(), role.getName());
        return role;
    }

    @Override
    @Cacheable(value = "roles", key = "'CUSTOMER'")
    public Role getCustomerRole() {
        log.info("Fetching CUSTOMER role");
        return getByName("CUSTOMER");
    }

    @Override
    @Cacheable(value = "roles", key = "'ADMIN'")
    public Role getAdminRole() {
        log.info("Fetching ADMIN role");
        return getByName("ADMIN");
    }

    @Override
    @Cacheable(value = "roles", key = "'SERVICE_PROVIDER'")
    public Role getServiceProviderRole() {
        log.info("Fetching SERVICE_PROVIDER role");
        return getByName("SERVICE_PROVIDER");
    }

}
