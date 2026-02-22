package com.sheemab.CQRS.QueryBus;


import com.sheemab.CQRS.query.GetAllProductsQuery;
import com.sheemab.CQRS.query.GetProductByIdQuery;
import com.sheemab.CQRS.query.GetProductsByCategoryQuery;
import com.sheemab.CQRS.query_handler.GetAllProductsQueryHandler;
import com.sheemab.CQRS.query_handler.GetProductByIdQueryHandler;
import com.sheemab.CQRS.query_handler.GetProductsByCategoryQueryHandler;
import com.sheemab.CQRS.query_response.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * QUERY BUS — the single entry point for all queries.
 *
 * Same benefits as the CommandBus:
 *   - Decouples controller from handler
 *   - Single place for cross-cutting concerns (caching, logging, authorization)
 *   - Easy to mock in tests
 *   - Easy to refactor handlers
 *
 * Key CQRS rule:
 *   Queries return data. They NEVER modify state.
 *   All query methods here are read-only operations.
 */
@Component
@RequiredArgsConstructor
public class QueryBus {

    private final GetProductByIdQueryHandler getByIdHandler;
    private final GetAllProductsQueryHandler getAllHandler;
    private final GetProductsByCategoryQueryHandler getByCategoryHandler;

    public ProductSummary dispatch(GetProductByIdQuery query) {
        return getByIdHandler.handle(query);
    }

    public List<ProductSummary> dispatch(GetAllProductsQuery query) {
        return getAllHandler.handle(query);
    }

    public List<ProductSummary> dispatch(GetProductsByCategoryQuery query) {
        return getByCategoryHandler.handle(query);
    }
}
