package com.sheemab.CQRS.exception;



/**
 * Thrown when trying to create a product that already exists
 * with the same name and category combination.
 */
public class DuplicateProductException extends RuntimeException {

    public DuplicateProductException(String message) {
        super(message);
    }
}
