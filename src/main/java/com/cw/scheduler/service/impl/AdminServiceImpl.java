package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ApiResponse.success(toDto(users), "Fetched all users.");
    }

    @Override
    public ApiResponse<List<UserResponseDTO>> getUsersByRole(String roleName) {
        List<User> users = userRepository.findUsersByRole(roleName);
        return ApiResponse.success(toDto(users), "Fetched users with role: " + roleName);
    }

    @Override
    public ApiResponse<List<UserResponseDTO>> getInactiveUsers() {
        List<User> users = userRepository.findInactiveUsers();
        return ApiResponse.success(toDto(users), "Fetched inactive users.");
    }

    @Override
    public ApiResponse<List<UserResponseDTO>> getActiveUsers() {
        List<User> users = userRepository.findByActiveTrue();
        return ApiResponse.success(toDto(users), "Fetched active users.");
    }

    @Override
    public ApiResponse<String> deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        return ApiResponse.success("User deactivated successfully.");
    }

    @Override
    public ApiResponse<String> activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(true);
        userRepository.save(user);
        return ApiResponse.success("User activated successfully.");
    }

    @Override
    public ApiResponse<String> deleteUserPermanently(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
        return ApiResponse.success("User permanently deleted.");
    }

    @Override
    public ApiResponse<UserResponseDTO> getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return ApiResponse.success(modelMapper.map(user, UserResponseDTO.class), "Fetched user details.");
    }

    private List<UserResponseDTO> toDto(List<User> users) {
        return users.stream()
                .map(user -> modelMapper.map(user, UserResponseDTO.class))
                .collect(Collectors.toList());
    }
}
