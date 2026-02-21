package com.sheemab.CQRS.events;



import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DOMAIN EVENT — published by the write side after a product is updated.
 * The read side listens and updates its ProductView accordingly.
 */
@Getter
@AllArgsConstructor
public class ProductUpdatedEvent {
    private final String        productId;
    private final String        name;
    private final String        category;
    private final BigDecimal    price;
    private final Integer       stock;
    private final String        description;
    private final LocalDateTime updatedAt;
}
