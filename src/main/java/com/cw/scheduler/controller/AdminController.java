package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin APIs")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @RateLimit(capacity = 20, refillTokens = 5, refillDurationSeconds = 60)
    @Operation(summary = "Get all users", description = "Fetches the list of all registered users in the system.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @RateLimit(capacity = 15, refillTokens = 3, refillDurationSeconds = 60)
    @Operation(summary = "Get users by role", description = "Fetches users filtered by a specific role.")
    @GetMapping("/role")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get inactive users", description = "Retrieves users who are currently inactive.")
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getInactiveUsers() {
        return ResponseEntity.ok(adminService.getInactiveUsers());
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get active users", description = "Retrieves users who are currently active.")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getActiveUsers() {
        return ResponseEntity.ok(adminService.getActiveUsers());
    }

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Deactivate a user", description = "Deactivates a user account by user ID.")
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deactivateUser(userId));
    }

    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Activate a user", description = "Activates a user account by user ID.")
    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateUser(userId));
    }

    @RateLimit(capacity = 3, refillTokens = 1, refillDurationSeconds = 60)
    @Operation(summary = "Delete a user permanently", description = "Deletes a user account permanently from the system.")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deleteUserPermanently(userId));
    }

    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information of a user by their ID.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }
}
