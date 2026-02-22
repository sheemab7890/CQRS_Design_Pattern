package com.sheemab.CQRS.query;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * QUERY — represents the INTENT to fetch a single product by ID.
 *
 * Key CQRS rule: Queries are questions ("give me this data").
 * They carry only what is needed to answer the question.
 * They NEVER modify state — purely read-only.
 */
@Getter
@AllArgsConstructor
public class GetProductByIdQuery {
    private final String productId;
}