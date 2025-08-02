package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.response.UserResponseDTO;
import com.cw.scheduler.service.interfaces.AdminService;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/role")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsersByRole(@RequestParam String role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getInactiveUsers() {
        return ResponseEntity.ok(adminService.getInactiveUsers());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getActiveUsers() {
        return ResponseEntity.ok(adminService.getActiveUsers());
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deactivateUser(userId));
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.activateUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deleteUserPermanently(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }
}
