package com.cw.scheduler.service.impl;

import com.cw.scheduler.advice.ApiResponse;
import com.cw.scheduler.dto.request.CategoryRequestDTO;
import com.cw.scheduler.dto.response.CategoryResponseDTO;
import com.cw.scheduler.dto.response.CategoryWithServiceCountDTO;
import com.cw.scheduler.entity.Category;
import com.cw.scheduler.exception.DuplicateResourceException;
import com.cw.scheduler.exception.ResourceNotFoundException;
import com.cw.scheduler.repository.CategoryRepository;
import com.cw.scheduler.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<CategoryResponseDTO> addCategory(CategoryRequestDTO request) {
        if(categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category" + request.getName() + "  is present");
        }
        Category category = new Category();
        category.setName(request.getName());

        CategoryResponseDTO response = modelMapper.map(categoryRepository.save(category), CategoryResponseDTO.class);
        return ApiResponse.success(response, "Category added successfully.");
    }

    @Override
    public ApiResponse<List<CategoryResponseDTO>> getAllCategory() {
        List<CategoryResponseDTO> response = categoryRepository.findAll()
                .stream().map(category -> modelMapper.map(category, CategoryResponseDTO.class))
                .toList();
        return ApiResponse.success(response, "Categories fetched successfully.");
    }

    @Override
    public ApiResponse<String> deleteCategory(Long categoryId) {
        if(!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not present with id : " + categoryId);
        }

        categoryRepository.deleteById(categoryId);

        return ApiResponse.success("Category deleted successfully.");
    }

    @Override
    public ApiResponse<List<CategoryWithServiceCountDTO>> getCategoriesWithServiceCount() {
        List<CategoryWithServiceCountDTO> list = categoryRepository.getCategoriesWithServiceCount();

        if (list == null || list.isEmpty()) {
            return ApiResponse.success(Collections.emptyList(), "No categories found.");
        }

        return ApiResponse.success(list, "Categories with service count");
    }
}
