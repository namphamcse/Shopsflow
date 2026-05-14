package dev.namphamcse.shopsflow.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.namphamcse.shopsflow.dto.response.OrderResponse;
import dev.namphamcse.shopsflow.entity.CartItem;
import dev.namphamcse.shopsflow.entity.Order;
import dev.namphamcse.shopsflow.entity.OrderItem;
import dev.namphamcse.shopsflow.entity.Product;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.entity.enums.OrderStatus;
import dev.namphamcse.shopsflow.entity.enums.Role;
import dev.namphamcse.shopsflow.exception.BusinessRuleViolationException;
import dev.namphamcse.shopsflow.exception.ResourceNotFoundException;
import dev.namphamcse.shopsflow.mapper.OrderMapper;
import dev.namphamcse.shopsflow.repository.CartItemRepository;
import dev.namphamcse.shopsflow.repository.OrderRepository;
import dev.namphamcse.shopsflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductRepository productRepo;

    @Transactional
    public OrderResponse placeOrder(User user) {
        List<CartItem> cartItems = cartItemRepo.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new BusinessRuleViolationException("Cannot place order with an empty cart");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessRuleViolationException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepo.save(product);

            BigDecimal priceAtPurchase = product.getPrice();
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    priceAtPurchase);
            orderItems.add(orderItem);

            BigDecimal itemSubtotal = priceAtPurchase.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);
        }

        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        cartItemRepo.deleteByUser(user);

        return OrderMapper.toOrderResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(User user) {
        return orderRepo.findByUserOrderByCreatedAtDesc(user).stream()
                .map(OrderMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse getOrderById(User user, Long id) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Order not found: " + id);
        }

        return OrderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        order.setStatus(status);
        return OrderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> findAllOrders() {
        return orderRepo.findAll()
            .stream()
            .map(OrderMapper::toOrderResponse)
            .toList();
    }
}
