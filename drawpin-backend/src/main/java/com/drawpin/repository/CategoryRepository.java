package com.drawpin.repository;

import com.drawpin.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);
    
    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Category> findByActiveTrueOrderByDisplayOrderAsc();

    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    List<Category> findByParentIdOrderByDisplayOrderAsc(UUID parentId);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.active = true ORDER BY c.displayOrder ASC")
    List<Category> searchByNameIgnoreCaseAndActiveTrue(@Param("name") String name);
}
