package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.LoginRequestDTO;
import com.cw.scheduler.dto.request.RegisterRequestDTO;
import com.cw.scheduler.dto.response.LoginResponseDTO;
import com.cw.scheduler.dto.response.UserResponseDTO;

public interface AuthService {
    ApiResponse<UserResponseDTO> register(RegisterRequestDTO request, String appUrl);
    ApiResponse<LoginResponseDTO> login(LoginRequestDTO request);
}
