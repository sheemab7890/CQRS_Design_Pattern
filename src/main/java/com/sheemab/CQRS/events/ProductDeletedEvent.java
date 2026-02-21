package com.sheemab.CQRS.events;



import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * DOMAIN EVENT — published by the write side after a product is deleted.
 * The read side listens and removes the ProductView entry.
 */
@Getter
@AllArgsConstructor
public class ProductDeletedEvent {
    private final String productId;
}
