package com.sheemab.CQRS.query;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * QUERY — represents the INTENT to fetch all products in a category.
 */
@Getter
@AllArgsConstructor
public class GetProductsByCategoryQuery {
    private final String category;
}
