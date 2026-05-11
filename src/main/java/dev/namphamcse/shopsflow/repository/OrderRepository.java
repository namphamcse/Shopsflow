package dev.namphamcse.shopsflow.repository;

import dev.namphamcse.shopsflow.entity.Order;
import dev.namphamcse.shopsflow.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
