package dev.namphamcse.shopsflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.namphamcse.shopsflow.dto.request.AddToCartRequest;
import dev.namphamcse.shopsflow.dto.request.UpdateCartItemRequest;
import dev.namphamcse.shopsflow.dto.response.CartResponse;
import dev.namphamcse.shopsflow.entity.CartItem;
import dev.namphamcse.shopsflow.entity.Product;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.exception.BusinessRuleViolationException;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.mapper.CartMapper;
import dev.namphamcse.shopsflow.repository.CartItemRepository;
import dev.namphamcse.shopsflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartService {
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;

    public CartResponse getCart(User user) {
        List<CartItem> items = cartItemRepo.findByUser(user);
        return CartMapper.toCartResponse(items);
    }

    @Transactional
    public CartResponse addToCart(User user, AddToCartRequest req) {
        Product p = productRepo.findById(req.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

        CartItem item = cartItemRepo.findByUserAndProduct(user, p)
            .orElseGet(() -> new CartItem(user, p, 0));

        int newQuantity = item.getQuantity() + req.getQuantity();
        if (newQuantity > p.getStockQuantity()) {
            throw new BusinessRuleViolationException("Requested quantity exceeds available stock: " + p.getStockQuantity());
        }
        item.setQuantity(newQuantity);
        cartItemRepo.save(item);

        return getCart(user);
    }
    
    @Transactional
    public CartResponse updateItem(User user, Long cartItemId, UpdateCartItemRequest req) {
        CartItem item = cartItemRepo.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
        }

        Product p = item.getProduct();
        if (req.getQuantity() > p.getStockQuantity()) {
            throw new BusinessRuleViolationException("Requested quantity exceeds available stock: " + p.getStockQuantity());
        }

        item.setQuantity(req.getQuantity());
        cartItemRepo.save(item);

        return getCart(user);
    }

    @Transactional
    public CartResponse removeItem(User user, Long cartItemId) {
        CartItem item = cartItemRepo.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cart item not found: " + cartItemId);
        }

        cartItemRepo.delete(item);
        return getCart(user);
    }

    @Transactional ///
    public void clearCart (User user) {
        cartItemRepo.deleteByUser(user);
    }
}
