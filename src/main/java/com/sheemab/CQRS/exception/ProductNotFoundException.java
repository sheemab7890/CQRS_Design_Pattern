package com.sheemab.CQRS.exception;



/**
 * Thrown when a product is not found by its ID.
 * Used on both command and query sides.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
