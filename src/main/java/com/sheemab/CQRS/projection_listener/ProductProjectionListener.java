package com.sheemab.CQRS.projection_listener;

import com.sheemab.CQRS.events.ProductCreatedEvent;
import com.sheemab.CQRS.events.ProductDeletedEvent;
import com.sheemab.CQRS.events.ProductUpdatedEvent;
import com.sheemab.CQRS.query_entity.ProductView;
import com.sheemab.CQRS.repository.ProductReadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * PROJECTION LISTENER — the bridge between the write side and read side.
 *
 * This is the heart of CQRS eventual consistency.
 *
 * How it works:
 *   1. Command Handler saves to write DB (products table)
 *   2. Command Handler publishes a domain event (Spring ApplicationEvent)
 *   3. THIS class listens to that event via @EventListener
 *   4. It updates the product_views table to match the new write state
 *
 * Result: the read model is always eventually consistent with the write model.
 * In this demo it's synchronous (same JVM, same thread).
 * In production you'd use Kafka/RabbitMQ for async cross-service projections.
 *
 * Key rule: This is the ONLY place that writes to product_views.
 * No command handler, no query handler, no controller ever writes to product_views.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductProjectionListener {

    private final ProductReadRepository readRepository;

    // ── Handle ProductCreatedEvent ────────────────────────────────────────────

    @EventListener
    @Transactional
    public void on(ProductCreatedEvent event) {
        log.info("Projecting ProductCreatedEvent: productId={}", event.getProductId());

        ProductView view = ProductView.builder()
                .id(event.getProductId())
                .name(event.getName())
                .category(event.getCategory())
                .price(event.getPrice())
                .stock(event.getStock())
                .description(event.getDescription())
                .active(true)
                .stockStatus(resolveStockStatus(event.getStock()))
                .createdAt(event.getCreatedAt())
                .lastUpdatedAt(event.getCreatedAt())
                .build();

        readRepository.save(view);
        log.info("ProductView created for productId={}", event.getProductId());
    }

    // ── Handle ProductUpdatedEvent ────────────────────────────────────────────

    @EventListener
    @Transactional
    public void on(ProductUpdatedEvent event) {
        log.info("Projecting ProductUpdatedEvent: productId={}", event.getProductId());

        ProductView view = readRepository.findById(event.getProductId())
                .orElseGet(() -> {
                    // Safety net: if the view somehow doesn't exist, create it
                    log.warn("ProductView not found for update, creating: productId={}",
                            event.getProductId());
                    return ProductView.builder()
                            .id(event.getProductId())
                            .createdAt(LocalDateTime.now())
                            .build();
                });

        // Apply the update to the read model
        view.setName(event.getName());
        view.setCategory(event.getCategory());
        view.setPrice(event.getPrice());
        view.setStock(event.getStock());
        view.setDescription(event.getDescription());
        view.setStockStatus(resolveStockStatus(event.getStock()));
        view.setLastUpdatedAt(event.getUpdatedAt());

        readRepository.save(view);
        log.info("ProductView updated for productId={}", event.getProductId());
    }

    // ── Handle ProductDeletedEvent ────────────────────────────────────────────

    @EventListener
    @Transactional
    public void on(ProductDeletedEvent event) {
        log.info("Projecting ProductDeletedEvent: productId={}", event.getProductId());

        readRepository.findById(event.getProductId()).ifPresent(view -> {
            // Soft delete — mark inactive, keep the record for history
            view.setActive(false);
            view.setLastUpdatedAt(LocalDateTime.now());
            readRepository.save(view);
            log.info("ProductView soft-deleted for productId={}", event.getProductId());
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String resolveStockStatus(int stock) {
        return stock > 0 ? "IN STOCK" : "OUT OF STOCK";
    }
}