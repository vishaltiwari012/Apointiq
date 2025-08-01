package com.cw.scheduler.repository;

import com.cw.scheduler.dto.response.CategoryWithServiceCountDTO;
import com.cw.scheduler.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query("SELECT new com.cw.scheduler.dto.response.CategoryWithServiceCountDTO(c.id, c.name, COUNT(s))" +
            "FROM Category c LEFT JOIN OfferedService s ON s.category = c " +
            "GROUP BY c.id, c.name"
    )
    List<CategoryWithServiceCountDTO> getCategoriesWithServiceCount();
}
