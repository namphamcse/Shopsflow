package dev.namphamcse.shopsflow.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
import dev.namphamcse.shopsflow.repository.spec.ProductSpecifications;
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

    public Page<ProductResponse> searchProducts(
        String keyword, Long categoryId,
        BigDecimal minPrice, BigDecimal maxPrice,
        Pageable pageable) {

        Specification<Product> spec = Stream.of(
                ProductSpecifications.hasKeyword(keyword),
                ProductSpecifications.inCategory(categoryId),
                ProductSpecifications.priceAtLeast(minPrice),
                ProductSpecifications.priceAtMost(maxPrice))
            .filter(Objects::nonNull)
            .reduce(Specification::and)
            .orElse((root, query, cb) -> cb.conjunction());

        return productRepo.findAll(spec, pageable)
                .map(ProductMapper::toResponse);
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
