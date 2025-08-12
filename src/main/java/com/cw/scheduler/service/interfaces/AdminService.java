package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.UserResponseDTO;

import java.util.List;

public interface AdminService {
    ApiResponse<List<UserResponseDTO>> getAllUsers();
    ApiResponse<List<UserResponseDTO>> getUsersByRole(String roleName);
    ApiResponse<List<UserResponseDTO>> getInactiveUsers();
    ApiResponse<List<UserResponseDTO>> getActiveUsers();
    ApiResponse<String> deactivateUser(Long userId);
    ApiResponse<String> activateUser(Long userId);
    ApiResponse<String> deleteUserPermanently(Long userId);
    ApiResponse<UserResponseDTO> getUserById(Long userId);
}
