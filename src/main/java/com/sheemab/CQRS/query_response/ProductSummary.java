package com.sheemab.CQRS.query_response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * QUERY RESPONSE DTO — what the client receives after a query.
 *
 * Key CQRS rule:
 *   This is NOT the ProductView entity, and NOT the Product write entity.
 *   It is a purpose-built DTO shaped exactly for the API response.
 *   This means we can change the internal read model without breaking the API,
 *   and change the API response without changing the internal models.
 *
 *   Three separate shapes:
 *     Product       (write model  — has domain logic)
 *     ProductView   (read model   — optimized for DB querying)
 *     ProductSummary (response DTO — shaped for the client)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummary {
    private String        id;
    private String        name;
    private String        category;
    private BigDecimal    price;
    private Integer       stock;
    private String        stockStatus;
    private String        description;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
