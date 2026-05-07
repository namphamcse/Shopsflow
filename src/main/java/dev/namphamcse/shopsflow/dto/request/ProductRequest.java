package dev.namphamcse.shopsflow.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal price;

    @Size(max = 500)
    private String imageUrl;
    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;

    private List<Long> categoryIds;
}
