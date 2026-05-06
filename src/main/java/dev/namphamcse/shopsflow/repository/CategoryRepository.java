package dev.namphamcse.shopsflow.repository;

import dev.namphamcse.shopsflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
