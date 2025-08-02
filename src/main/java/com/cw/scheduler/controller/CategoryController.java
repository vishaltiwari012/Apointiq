package com.cw.scheduler.controller;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CategoryRequestDTO;
import com.cw.scheduler.dto.response.CategoryResponseDTO;
import com.cw.scheduler.dto.response.CategoryWithServiceCountDTO;
import com.cw.scheduler.service.interfaces.CategoryService;
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

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> addCategory(@Valid @RequestBody CategoryRequestDTO requestDTO) {
        return new ResponseEntity<>(categoryService.addCategory(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategory() {
        return new ResponseEntity<>(categoryService.getAllCategory(), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        return new ResponseEntity<>(categoryService.deleteCategory(id), HttpStatus.OK);
    }

    @GetMapping("/with-service-count")
    public ResponseEntity<ApiResponse<List<CategoryWithServiceCountDTO>>> getCategoriesWithServiceCount() {
        return ResponseEntity.ok(categoryService.getCategoriesWithServiceCount());
    }

}
