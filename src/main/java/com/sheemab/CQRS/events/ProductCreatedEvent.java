package com.sheemab.CQRS.events;



import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DOMAIN EVENT — published by the write side after a product is created.
 * The read side listens to this and updates the ProductView table.
 *
 * This is what keeps the read model eventually consistent
 * with the write model — no direct DB sharing needed.
 */
@Getter
@AllArgsConstructor
public class ProductCreatedEvent {
    private final String        productId;
    private final String        name;
    private final String        category;
    private final BigDecimal    price;
    private final Integer       stock;
    private final String        description;
    private final LocalDateTime createdAt;
}
