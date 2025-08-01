package com.cw.scheduler.service.interfaces;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CategoryRequestDTO;
import com.cw.scheduler.dto.response.CategoryResponseDTO;
import com.cw.scheduler.dto.response.CategoryWithServiceCountDTO;

import java.util.List;

public interface CategoryService {
    ApiResponse<CategoryResponseDTO> addCategory(CategoryRequestDTO request);
    ApiResponse<List<CategoryResponseDTO>> getAllCategory();
    ApiResponse<String> deleteCategory(Long categoryId);
    ApiResponse<List<CategoryWithServiceCountDTO>> getCategoriesWithServiceCount();
}
