package dev.namphamcse.shopsflow.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.namphamcse.shopsflow.dto.response.OrderResponse;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.placeOrder(user));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getUserOrders(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(user, id));
    }
}
