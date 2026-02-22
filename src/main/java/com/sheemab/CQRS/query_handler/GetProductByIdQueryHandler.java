package com.sheemab.CQRS.query_handler;


import com.sheemab.CQRS.exception.ProductNotFoundException;
import com.sheemab.CQRS.query.GetProductByIdQuery;
import com.sheemab.CQRS.query_entity.ProductView;
import com.sheemab.CQRS.query_response.ProductSummary;
import com.sheemab.CQRS.repository.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * QUERY HANDLER — handles GetProductByIdQuery.
 *
 * Key CQRS rules:
 *   1. Reads ONLY from the ProductReadRepository (product_views table).
 *      It never touches the write-side products table.
 *
 *   2. Returns a ProductSummary DTO, not the raw ProductView entity.
 *      The internal model and the API response stay decoupled.
 *
 *   3. @Transactional(readOnly = true) — tells JPA this is a read-only
 *      operation. Hibernate skips dirty-checking, Spring may route to
 *      a read replica in a production setup. Zero writes can happen here.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetProductByIdQueryHandler {

    private final ProductReadRepository readRepository;

    @Transactional(readOnly = true)
    public ProductSummary handle(GetProductByIdQuery query) {
        log.debug("Handling GetProductByIdQuery: productId={}", query.getProductId());

        ProductView view = readRepository.findByIdAndActiveTrue(query.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(query.getProductId()));

        return toSummary(view);
    }

    // ── Mapping — keeps JPA entity internals out of the API response ──────────
    private ProductSummary toSummary(ProductView view) {
        return ProductSummary.builder()
                .id(view.getId())
                .name(view.getName())
                .category(view.getCategory())
                .price(view.getPrice())
                .stock(view.getStock())
                .stockStatus(view.getStockStatus())
                .description(view.getDescription())
                .createdAt(view.getCreatedAt())
                .lastUpdatedAt(view.getLastUpdatedAt())
                .build();
    }
}
