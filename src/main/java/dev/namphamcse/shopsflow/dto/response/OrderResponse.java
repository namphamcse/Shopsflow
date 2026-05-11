package dev.namphamcse.shopsflow.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import dev.namphamcse.shopsflow.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
