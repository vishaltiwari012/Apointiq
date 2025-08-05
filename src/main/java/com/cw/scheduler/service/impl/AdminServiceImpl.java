package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.entity.User;
import com.cw.scheduler.exception.UserNotFoundException;
import com.cw.scheduler.repository.UserRepository;
import com.cw.scheduler.service.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(value = "allUsers")
    public ApiResponse<List<UserResponseDTO>> getAllUsers() {
        log.info("Fetching all users from DB");
        List<User> users = userRepository.findAll();
        return ApiResponse.success(toDto(users), "Fetched all users.");
    }

    @Override
    @Cacheable(value = "usersByRole", key = "#roleName")
    public ApiResponse<List<UserResponseDTO>> getUsersByRole(String roleName) {
        log.info("Fetching users with role: {}", roleName);
        List<User> users = userRepository.findUsersByRole(roleName);
        return ApiResponse.success(toDto(users), "Fetched users with role: " + roleName);
    }

    @Override
    @Cacheable(value = "inactiveUsers")
    public ApiResponse<List<UserResponseDTO>> getInactiveUsers() {
        log.info("Fetching inactive users");
        List<User> users = userRepository.findInactiveUsers();
        return ApiResponse.success(toDto(users), "Fetched inactive users.");
    }

    @Override
    @Cacheable(value = "activeUsers")
    public ApiResponse<List<UserResponseDTO>> getActiveUsers() {
        log.info("Fetching active users");
        List<User> users = userRepository.findByActiveTrue();
        return ApiResponse.success(toDto(users), "Fetched active users.");
    }

    @Override
    @CacheEvict(value = {"allUsers", "activeUsers", "inactiveUsers", "usersByRole", "userById"}, allEntries = true)
    public ApiResponse<String> deactivateUser(Long userId) {
        log.warn("Deactivating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        return ApiResponse.success("User deactivated successfully.");
    }

    @Override
    @CacheEvict(value = {"allUsers", "activeUsers", "inactiveUsers", "usersByRole", "userById"}, allEntries = true)
    public ApiResponse<String> activateUser(Long userId) {
        log.info("Activating user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setActive(true);
        userRepository.save(user);
        return ApiResponse.success("User activated successfully.");
    }

    @Override
    @CacheEvict(value = {"allUsers", "activeUsers", "inactiveUsers", "usersByRole", "userById"}, allEntries = true)
    public ApiResponse<String> deleteUserPermanently(Long userId) {
        log.error("Deleting user permanently with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(userId);
        return ApiResponse.success("User permanently deleted.");
    }

    @Override
    @Cacheable(value = "userById", key = "#userId")
    public ApiResponse<UserResponseDTO> getUserById(Long userId) {
        log.info("Fetching user details for ID: {}", userId);
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
