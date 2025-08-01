package com.cw.scheduler.util;


import com.cw.scheduler.entity.User;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.service.interfaces.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    @PostConstruct
    public void createAdminIfNotExists() {
        if (userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            log.info("Admin user already exists: {}", DEFAULT_ADMIN_EMAIL);
            return;
        }

        log.info("Creating default admin user: {}", DEFAULT_ADMIN_EMAIL);

        User user = new User();
        user.setName("Admin");
        user.setEmail(DEFAULT_ADMIN_EMAIL);
        user.setPhoneNumber("9999999999");
        user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        user.setRoles(Set.of(roleService.getAdminRole()));

        User savedUser = userRepository.save(user);

        log.info("Admin user created successfully: {}", savedUser.getEmail());
    }
}