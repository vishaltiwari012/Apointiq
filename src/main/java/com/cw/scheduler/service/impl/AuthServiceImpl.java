package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.LoginRequestDTO;
import com.cw.scheduler.dto.request.RegisterRequestDTO;
import com.cw.scheduler.dto.response.LoginResponseDTO;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.entity.Role;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.entity.enums.NotificationType;
import com.cw.scheduler.exception.BadCredentialsException;
import com.cw.scheduler.exception.DuplicateResourceException;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.security.CustomUserDetailsService;
import com.cw.scheduler.security.JwtService;
import com.cw.scheduler.service.interfaces.AuthService;
import com.cw.scheduler.service.interfaces.NotificationService;
import com.cw.scheduler.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ApiResponse<UserResponseDTO> register(RegisterRequestDTO request, String appUrl) {
        log.info("Registering new user: {}", request.getEmail());
        validateEmail(request.getEmail());

        User user = buildNewUser(request);
        User savedUser = userRepository.save(user);

        sendWelcomeEmail(savedUser, appUrl);

        log.info("User registered successfully: {}", savedUser.getEmail());

        UserResponseDTO response = mapToUserResponseDTO(savedUser);
        return ApiResponse.success(response, "User registered successfully.");
    }

    @Override
    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: Email not found - {}", request.getEmail());
                    return new UserNotFoundException("Invalid email");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email={}", request.getEmail());
            throw new BadCredentialsException("Invalid password");
        }

        String jwtToken = jwtService.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail()),
                user.getId()
        );
        String refreshToken = jwtService.generateRefreshToken(
                userDetailsService.loadUserByUsername(user.getEmail())
        );

        log.info("User logged in successfully: {}", user.getEmail());

        return ApiResponse.success(
                new LoginResponseDTO(user.getEmail(), jwtToken, refreshToken),
                "Login successful"
        );
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: Duplicate email={}", email);
            throw new DuplicateResourceException("User is already registered with: " + email);
        }
    }

    private User buildNewUser(RegisterRequestDTO request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(roleService.getCustomerRole()))
                .build();
    }

    @Transactional
    private void sendWelcomeEmail(User user, String appUrl) {
        log.debug("Sending welcome notification to userId={}", user.getId());

        notificationService.saveNotification(user, "Welcome to our platform!", NotificationType.REGISTRATION);

        String loginUrl = appUrl + "/auth/login";
        notificationService.sendEmail(
                user.getEmail(),
                "Welcome to Apointiq",
                "welcome-email",
                Map.of(
                        "username", user.getName(),
                        "loginUrl", loginUrl
                )
        );

        log.info("Welcome email sent to {}", user.getEmail());
    }

    private UserResponseDTO mapToUserResponseDTO(User user) {
        UserResponseDTO dto = modelMapper.map(user, UserResponseDTO.class);
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
