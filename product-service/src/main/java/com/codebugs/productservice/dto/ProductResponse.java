package com.codebugs.productservice.dto;

import com.codebugs.productservice.model.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String category,
    Instant createdAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(),
            p.getPrice(), p.getStock(), p.getCategory(), p.getCreatedAt());
    }
}
