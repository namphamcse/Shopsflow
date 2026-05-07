package dev.namphamcse.shopsflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.namphamcse.shopsflow.dto.request.ProductRequest;
import dev.namphamcse.shopsflow.dto.response.ProductResponse;
import dev.namphamcse.shopsflow.entity.Category;
import dev.namphamcse.shopsflow.entity.Product;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.mapper.ProductMapper;
import dev.namphamcse.shopsflow.repository.CategoryRepository;
import dev.namphamcse.shopsflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    private List<Category> resolveCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Category> categories = categoryRepo.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more categories not found: "
                    + ids);
        }
        return categories;
    }

    public ProductResponse getProductById(Long id) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return ProductMapper.toResponse(p);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepo.findAll()
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        List<Category> categories = resolveCategories(req.getCategoryIds());
        Product p = ProductMapper.toEntity(req, categories);
        Product saved = productRepo.save(p);
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product p = productRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        List<Category> categories = resolveCategories(req.getCategoryIds());
        ProductMapper.updateEntity(p, req, categories);
        Product saved = productRepo.save(p); 
        return ProductMapper.toResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepo.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepo.deleteById(id);
    }
}
