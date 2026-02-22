package com.sheemab.CQRS.controller;


import com.sheemab.CQRS.QueryBus.QueryBus;
import com.sheemab.CQRS.exception.ApiResponse;
import com.sheemab.CQRS.query.GetAllProductsQuery;
import com.sheemab.CQRS.query.GetProductByIdQuery;
import com.sheemab.CQRS.query.GetProductsByCategoryQuery;
import com.sheemab.CQRS.query_response.ProductSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QUERY CONTROLLER — handles all read operations (GET).
 *
 * Key CQRS rule demonstrated:
 *   This controller ONLY reads. It NEVER calls command handlers.
 *   If you want to modify data, you call the ProductCommandController.
 *
 * Responsibilities:
 *   1. Build a Query object
 *   2. Dispatch to the QueryBus
 *   3. Return the result wrapped in ApiResponse
 *
 * What it does NOT do:
 *   - Does NOT call any command handler
 *   - Does NOT modify state
 *   - All methods here are pure reads
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductQueryController {

    private final QueryBus queryBus;

    // ── GET /api/products/{id} ────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSummary>> getProductById(@PathVariable String id) {

        log.info("GET /api/products/{}", id);

        GetProductByIdQuery query = new GetProductByIdQuery(id);
        ProductSummary product = queryBus.dispatch(query);

        return ResponseEntity.ok(ApiResponse.success(product));
    }

    // ── GET /api/products?category=...&page=...&size=... ──────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSummary>>> getAllProducts(
            @RequestParam(required = false) String  category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        log.info("GET /api/products: category={}, page={}, size={}", category, page, size);

        GetAllProductsQuery query = new GetAllProductsQuery(category, page, size);
        List<ProductSummary> products = queryBus.dispatch(query);

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    // ── GET /api/products/category/{category} ─────────────────────────────────
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductSummary>>> getProductsByCategory(
            @PathVariable String category) {

        log.info("GET /api/products/category/{}", category);

        GetProductsByCategoryQuery query = new GetProductsByCategoryQuery(category);
        List<ProductSummary> products = queryBus.dispatch(query);

        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
