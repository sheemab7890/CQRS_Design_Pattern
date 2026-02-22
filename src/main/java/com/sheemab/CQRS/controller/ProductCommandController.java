package com.sheemab.CQRS.controller;


import com.sheemab.CQRS.CommandBus.CommandBus;
import com.sheemab.CQRS.command.CreateProductCommand;
import com.sheemab.CQRS.command.DeleteProductCommand;
import com.sheemab.CQRS.command.UpdateProductCommand;
import com.sheemab.CQRS.dto.CreateProductRequest;
import com.sheemab.CQRS.dto.UpdateProductRequest;
import com.sheemab.CQRS.exception.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * COMMAND CONTROLLER — handles all write operations (POST, PUT, DELETE).
 *
 * Key CQRS rule demonstrated:
 *   Commands go to one controller, queries to another.
 *   This is the "C" in CQRS — Command side only.
 *
 * Responsibilities:
 *   1. Validate incoming request (Spring does this via @Valid)
 *   2. Map DTO → Command
 *   3. Dispatch command to the CommandBus
 *   4. Return a minimal response (just the new ID for creates, or void)
 *
 * What it does NOT do:
 *   - Does NOT fetch the created/updated product and return it
 *   - Does NOT call any query handler
 *   - If the client wants to see the updated product, they issue a Query
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductCommandController {

    private final CommandBus commandBus;

    // ── POST /api/products ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("POST /api/products: name={}, category={}", request.getName(), request.getCategory());

        CreateProductCommand command = CreateProductCommand.builder()
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .build();

        String productId = commandBus.dispatch(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(productId, "Product created successfully"));
    }

    // ── PUT /api/products/{id} ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("PUT /api/products/{}: name={}", id, request.getName());

        UpdateProductCommand command = UpdateProductCommand.builder()
                .productId(id)
                .name(request.getName())
                .category(request.getCategory())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .build();

        commandBus.dispatch(command);

        return ResponseEntity.ok(ApiResponse.success(null, "Product updated successfully"));
    }

    // ── DELETE /api/products/{id} ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {

        log.info("DELETE /api/products/{}", id);

        DeleteProductCommand command = DeleteProductCommand.builder()
                .productId(id)
                .build();

        commandBus.dispatch(command);

        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}
