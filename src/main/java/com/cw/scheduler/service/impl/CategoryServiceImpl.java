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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categoriesWithServiceCount", allEntries = true)
    })
    public ApiResponse<CategoryResponseDTO> addCategory(CategoryRequestDTO request) {
        log.info("Attempting to add new category with name={}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Duplicate category creation attempt for name={}", request.getName());
            throw new DuplicateResourceException("Category " + request.getName() + " already exists");
        }

        Category category = new Category();
        category.setName(request.getName());

        Category savedCategory = categoryRepository.save(category);
        CategoryResponseDTO response = modelMapper.map(savedCategory, CategoryResponseDTO.class);

        log.debug("Category saved with id={} and name={}", savedCategory.getId(), savedCategory.getName());
        return ApiResponse.success(response, "Category added successfully.");
    }

    @Override
    @Cacheable(value = "categories")
    public ApiResponse<List<CategoryResponseDTO>> getAllCategory() {
        log.info("Fetching all categories");

        List<CategoryResponseDTO> response = categoryRepository.findAll()
                .stream()
                .map(category -> modelMapper.map(category, CategoryResponseDTO.class))
                .toList();

        log.debug("Found {} categories", response.size());
        return ApiResponse.success(response, "Categories fetched successfully.");
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "categoriesWithServiceCount", allEntries = true)
    })
    public ApiResponse<String> deleteCategory(Long categoryId) {
        log.info("Attempting to delete category with id={}", categoryId);

        if (!categoryRepository.existsById(categoryId)) {
            log.warn("Delete failed — category not found for id={}", categoryId);
            throw new ResourceNotFoundException("Category not present with id: " + categoryId);
        }

        categoryRepository.deleteById(categoryId);
        log.debug("Category deleted with id={}", categoryId);

        return ApiResponse.success("Category deleted successfully.");
    }

    @Override
    @Cacheable(value = "categoriesWithServiceCount")
    public ApiResponse<List<CategoryWithServiceCountDTO>> getCategoriesWithServiceCount() {
        log.info("Fetching categories with service counts");

        List<CategoryWithServiceCountDTO> list = categoryRepository.getCategoriesWithServiceCount();

        if (list == null || list.isEmpty()) {
            log.info("No categories found when fetching with service count");
            return ApiResponse.success(Collections.emptyList(), "No categories found.");
        }

        log.debug("Found {} categories with service counts", list.size());
        return ApiResponse.success(list, "Categories with service count");
    }
}
