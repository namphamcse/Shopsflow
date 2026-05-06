package dev.namphamcse.shopsflow.repository;

import dev.namphamcse.shopsflow.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
