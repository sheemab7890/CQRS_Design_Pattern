package com.sheemab.CQRS.command_handler;


import com.sheemab.CQRS.command.UpdateProductCommand;
import com.sheemab.CQRS.command_entity.Product;
import com.sheemab.CQRS.events.ProductUpdatedEvent;
import com.sheemab.CQRS.exception.ProductNotFoundException;
import com.sheemab.CQRS.repository.ProductWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * COMMAND HANDLER — handles UpdateProductCommand.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateProductCommandHandler {

    private final ProductWriteRepository writeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handle(UpdateProductCommand command) {
        log.info("Handling UpdateProductCommand: productId={}", command.getProductId());

        // ── Load the aggregate ────────────────────────────────────────────────
        Product product = writeRepository.findById(command.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));

        // ── Apply change via domain method (business logic stays in aggregate)
        product.update(
                command.getName(),
                command.getCategory(),
                command.getPrice(),
                command.getStock(),
                command.getDescription()
        );

        // ── Persist ───────────────────────────────────────────────────────────
        Product saved = writeRepository.save(product);
        log.info("Product updated on write side: id={}", saved.getId());

        // ── Publish domain event so read side syncs ───────────────────────────
        eventPublisher.publishEvent(new ProductUpdatedEvent(
                saved.getId(),
                saved.getName(),
                saved.getCategory(),
                saved.getPrice(),
                saved.getStock(),
                saved.getDescription(),
                saved.getUpdatedAt()
        ));
    }
}
