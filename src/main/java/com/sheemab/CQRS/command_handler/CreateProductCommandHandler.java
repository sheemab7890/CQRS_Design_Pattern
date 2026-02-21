package com.sheemab.CQRS.command_handler;




import com.sheemab.CQRS.command.CreateProductCommand;
import com.sheemab.CQRS.command_entity.Product;
import com.sheemab.CQRS.events.ProductCreatedEvent;
import com.sheemab.CQRS.repository.ProductWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * COMMAND HANDLER — handles CreateProductCommand.
 *
 * Responsibilities:
 *   1. Validate business rules (duplicate check)
 *   2. Build and persist the domain aggregate (Product)
 *   3. Publish a domain event so the READ side can sync
 *
 * Key CQRS rule:
 *   Command handlers return NOTHING meaningful to the caller
 *   (just the new ID here as a minimal acknowledgment).
 *   They do NOT return the full product — that's a query's job.
 *
 * One handler = one command. Never mix multiple commands in one handler.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateProductCommandHandler {

    private final ProductWriteRepository writeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public String handle(CreateProductCommand command) {
        log.info("Handling CreateProductCommand: name={}, category={}",
                command.getName(), command.getCategory());

        // ── Business rule: no duplicate name+category ─────────────────────────
        if (writeRepository.existsByNameAndCategory(command.getName(), command.getCategory())) {
            throw new DuplicateProductException(
                    "Product '%s' already exists in category '%s'"
                            .formatted(command.getName(), command.getCategory()));
        }

        // ── Build domain aggregate ────────────────────────────────────────────
        Product product = Product.builder()
                .name(command.getName())
                .category(command.getCategory())
                .price(command.getPrice())
                .stock(command.getStock())
                .description(command.getDescription())
                .build();

        // ── Persist to write DB ───────────────────────────────────────────────
        Product saved = writeRepository.save(product);
        log.info("Product created on write side: id={}", saved.getId());

        // ── Publish domain event so read side syncs ───────────────────────────
        eventPublisher.publishEvent(new ProductCreatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getCategory(),
                saved.getPrice(),
                saved.getStock(),
                saved.getDescription(),
                saved.getCreatedAt()
        ));

        return saved.getId();
    }
}
