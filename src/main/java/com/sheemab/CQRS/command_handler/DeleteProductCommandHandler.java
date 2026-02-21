package com.sheemab.CQRS.command_handler;


import com.sheemab.CQRS.command.DeleteProductCommand;
import com.sheemab.CQRS.command_entity.Product;
import com.sheemab.CQRS.events.ProductDeletedEvent;
import com.sheemab.CQRS.exception.ProductNotFoundException;
import com.sheemab.CQRS.repository.ProductWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * COMMAND HANDLER — handles DeleteProductCommand.
 *
 * We use soft delete (deactivate) rather than hard delete.
 * This preserves history and keeps the read model consistent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteProductCommandHandler {

    private final ProductWriteRepository writeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handle(DeleteProductCommand command) {
        log.info("Handling DeleteProductCommand: productId={}", command.getProductId());

        // ── Load the aggregate ────────────────────────────────────────────────
        Product product = writeRepository.findById(command.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(command.getProductId()));

        // ── Soft delete via domain method ─────────────────────────────────────
        product.deactivate();
        writeRepository.save(product);
        log.info("Product deactivated on write side: id={}", command.getProductId());

        // ── Publish domain event so read side syncs ───────────────────────────
        eventPublisher.publishEvent(new ProductDeletedEvent(command.getProductId()));
    }
}
