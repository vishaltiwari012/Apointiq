package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CategoryRequestDTO;
import com.cw.scheduler.dto.response.CategoryResponseDTO;
import com.cw.scheduler.dto.response.CategoryWithServiceCountDTO;
import com.cw.scheduler.ratelimit.RateLimit;
import com.cw.scheduler.service.interfaces.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category APIs")
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Add a new category", description = "Creates a new service category (ADMIN only).")
    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> addCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        return new ResponseEntity<>(categoryService.addCategory(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all categories", description = "Retrieves all service categories (ADMIN only).")
    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategory() {
        return new ResponseEntity<>(categoryService.getAllCategory(), HttpStatus.OK);
    }

    @Operation(summary = "Delete a category", description = "Removes a service category by ID (ADMIN only).")
    @RateLimit(capacity = 5, refillTokens = 1, refillDurationSeconds = 60)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        return new ResponseEntity<>(categoryService.deleteCategory(id), HttpStatus.OK);
    }

    @Operation(summary = "Get categories with service count", description = "Retrieves categories with service counts (ADMIN only).")
    @RateLimit(capacity = 10, refillTokens = 2, refillDurationSeconds = 60)
    @GetMapping("/with-service-count")
    public ResponseEntity<ApiResponse<List<CategoryWithServiceCountDTO>>> getCategoriesWithServiceCount() {
        return ResponseEntity.ok(categoryService.getCategoriesWithServiceCount());
    }
}

