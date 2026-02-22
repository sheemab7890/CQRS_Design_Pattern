package com.sheemab.CQRS.query_handler;


import com.sheemab.CQRS.query.GetProductsByCategoryQuery;
import com.sheemab.CQRS.query_response.ProductSummary;
import com.sheemab.CQRS.repository.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * QUERY HANDLER — handles GetProductsByCategoryQuery.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetProductsByCategoryQueryHandler {

    private final ProductReadRepository readRepository;

    @Transactional(readOnly = true)
    public List<ProductSummary> handle(GetProductsByCategoryQuery query) {
        log.debug("Handling GetProductsByCategoryQuery: category={}", query.getCategory());

        return readRepository
                .findByCategoryAndActiveTrue(query.getCategory())
                .stream()
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
                        .build())
                .toList();
    }
}
