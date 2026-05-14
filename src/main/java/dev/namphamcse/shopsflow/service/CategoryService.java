package dev.namphamcse.shopsflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.namphamcse.shopsflow.dto.request.CategoryRequest;
import dev.namphamcse.shopsflow.dto.response.CategoryResponse;
import dev.namphamcse.shopsflow.entity.Category;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.mapper.CategoryMapper;
import dev.namphamcse.shopsflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepo;

    public List<CategoryResponse> getAllCategories(){
        return categoryRepo.findAll()
        .stream()
        .map(CategoryMapper::toResponse)
        .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category c = categoryRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return CategoryMapper.toResponse(c);
    }

    @Transactional ///
    public CategoryResponse createCategory(CategoryRequest req) {
        if (categoryRepo.existsByName(req.getName())) {
            throw new ResourceNotFoundException("Category not found: " + req.getName());
        }
        Category c = CategoryMapper.toEntity(req);
        Category saved = categoryRepo.save(c);
        return CategoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest req){
        Category c = categoryRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        CategoryMapper.updateEntity(c, req);
        Category saved = categoryRepo.save(c);
        return CategoryMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category c = categoryRepo.findById(id).
            orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        if (!c.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with products attached");
        }
        categoryRepo.delete(c);
    }

}
