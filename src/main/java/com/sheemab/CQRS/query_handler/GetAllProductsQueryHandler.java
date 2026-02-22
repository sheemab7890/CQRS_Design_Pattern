package com.sheemab.CQRS.query_handler;


import com.sheemab.CQRS.query.GetAllProductsQuery;
import com.sheemab.CQRS.query_response.ProductSummary;
import com.sheemab.CQRS.repository.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * QUERY HANDLER — handles GetAllProductsQuery.
 *
 * Supports optional pagination (page + size).
 * Defaults to page 0, size 20 if not provided.
 * Always sorted by name ascending for consistent results.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetAllProductsQueryHandler {

    private final ProductReadRepository readRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    @Transactional(readOnly = true)
    public List<ProductSummary> handle(GetAllProductsQuery query) {
        log.debug("Handling GetAllProductsQuery: page={}, size={}, category={}",
                query.getPage(), query.getSize(), query.getCategory());

        int page = query.getPage() != null ? query.getPage() : DEFAULT_PAGE;
        int size = query.getSize() != null ? query.getSize() : DEFAULT_SIZE;

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductSummary> result = readRepository
                .findByActiveTrue(pageable)
                .map(view -> ProductSummary.builder()
                        .id(view.getId())
                        .name(view.getName())
                        .category(view.getCategory())
                        .price(view.getPrice())
                        .stock(view.getStock())
                        .stockStatus(view.getStockStatus())
                        .description(view.getDescription())
                        .createdAt(view.getCreatedAt())
                        .lastUpdatedAt(view.getLastUpdatedAt())
                        .build());

        log.debug("GetAllProductsQuery returned {} products (total={})",
                result.getNumberOfElements(), result.getTotalElements());

        return result.getContent();
    }
}
