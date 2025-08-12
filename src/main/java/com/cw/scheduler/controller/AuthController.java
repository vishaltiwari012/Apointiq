package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.LoginRequestDTO;
import com.cw.scheduler.dto.request.RegisterRequestDTO;
import com.cw.scheduler.dto.response.LoginResponseDTO;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth APIs")
public class AuthController {

    private final AuthService authService;

    @RateLimit(capacity = 5, refillTokens = 5, refillDurationSeconds = 60, message = "Too many registration attempts. Please wait a minute.")
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO, HttpServletRequest servletRequest) {
        String appUrl = getApplicationUrl(servletRequest);
        return new ResponseEntity<>(authService.register(registerRequestDTO, appUrl), HttpStatus.CREATED);
    }

    @RateLimit(capacity = 10, refillTokens = 10, refillDurationSeconds = 60, message = "Too many login attempts. Please wait a minute.")
    @PostMapping("/login")
    @Operation(summary = "User login with email and password", description = "Verifies credentials")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        ApiResponse<LoginResponseDTO> apiResponse= authService.login(loginRequestDTO);
        setRefreshTokenCookie(response, apiResponse.getData().getRefreshToken());
        apiResponse.getData().setRefreshToken("Securely saved");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true in production
        cookie.setPath("/api/v1/auth/refresh-token");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);
    }

    private String getApplicationUrl(HttpServletRequest request) {
        return request.getRequestURL().toString().replace(request.getServletPath(), "");
    }
}
