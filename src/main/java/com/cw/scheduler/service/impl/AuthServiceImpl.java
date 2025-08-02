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
    public ApiResponse<UserResponseDTO> register(RegisterRequestDTO request, String appUrl) {
        log.info("Registering user: {}", request.getEmail());
        validateEmail(request.getEmail());
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(roleService.getCustomerRole()));

        User savedUser = userRepository.save(user);

        sendWelcomeEmail(savedUser, appUrl);

        log.info("User registered successfully: {}", user.getEmail());

        UserResponseDTO response = modelMapper.map(savedUser, UserResponseDTO.class);
        response.setRoles(savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        return ApiResponse.success(response, "User registered successfully.");
    }

    @Override
    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid email"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String jwtToken = jwtService.generateToken(
                userDetailsService.loadUserByUsername(user.getEmail()),
                user.getId()
        );
        String refreshToken = jwtService.generateRefreshToken(userDetailsService.loadUserByUsername(user.getEmail()));

        log.info("User logged in successfully: {}", user.getEmail());

        LoginResponseDTO response = new LoginResponseDTO(user.getEmail(), jwtToken, refreshToken);

        return ApiResponse.success(response, "Login successful");

    }

    private void validateEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User is already registered with : " + email);
        }
    }

    @Transactional
    private void sendWelcomeEmail(User user, String appUrl) {
        notificationService.saveNotification(user, "Welcome to our platform!", NotificationType.REGISTRATION);
        String loginUrl = appUrl + "/auth/login";
        notificationService.sendEmail(
                user.getEmail(),
                "Welcome to Apointiq",
                "welcome-email",
                Map.of(
                        "username", user.getName(),
                        "loginUrl", loginUrl
                ));
        log.info("Welcome Email sent to {}", user.getEmail());
    }

}
