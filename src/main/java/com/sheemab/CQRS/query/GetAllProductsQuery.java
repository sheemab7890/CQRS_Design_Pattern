package com.sheemab.CQRS.query;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * QUERY — represents the INTENT to fetch all active products
 * with optional filtering and pagination.
 *
 * Queries can carry filter/sort/page parameters.
 * The handler decides how to apply them — the caller just asks a question.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetAllProductsQuery {
    private String  category;       // optional filter by category
    private Integer page;           // optional pagination (0-based)
    private Integer size;           // optional page size
}