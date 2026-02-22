package com.sheemab.CQRS.CommandBus;




import com.sheemab.CQRS.command.CreateProductCommand;
import com.sheemab.CQRS.command.DeleteProductCommand;
import com.sheemab.CQRS.command.UpdateProductCommand;
import com.sheemab.CQRS.command_handler.CreateProductCommandHandler;
import com.sheemab.CQRS.command_handler.DeleteProductCommandHandler;
import com.sheemab.CQRS.command_handler.UpdateProductCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * COMMAND BUS — the single entry point for all commands.
 *
 * Why use a bus instead of calling handlers directly?
 *
 *   1. Single Responsibility — the controller just hands off the command,
 *      it doesn't know which handler processes it.
 *
 *   2. Testability — you can mock the bus in controller tests without
 *      setting up the entire handler dependency tree.
 *
 *   3. Cross-cutting concerns — in production you'd add logging, metrics,
 *      authorization, transaction boundaries, retry logic here. Every
 *      command automatically gets these without touching individual handlers.
 *
 *   4. Easy refactoring — if you change a handler's signature or swap
 *      implementations, the controller never changes.
 *
 * Note: In a real enterprise system you'd use a framework like Axon or
 * MediatR. This is a lightweight hand-rolled version for learning.
 */
@Component
@RequiredArgsConstructor
public class CommandBus {

    private final CreateProductCommandHandler createHandler;
    private final UpdateProductCommandHandler updateHandler;
    private final DeleteProductCommandHandler deleteHandler;

    public String dispatch(CreateProductCommand command) {
        return createHandler.handle(command);
    }

    public void dispatch(UpdateProductCommand command) {
        updateHandler.handle(command);
    }

    public void dispatch(DeleteProductCommand command) {
        deleteHandler.handle(command);
    }
}
