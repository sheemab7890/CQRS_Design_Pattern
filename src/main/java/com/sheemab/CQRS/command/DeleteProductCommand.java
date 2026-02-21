package com.sheemab.CQRS.command;



import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COMMAND — represents the INTENT to delete a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductCommand {

    @NotBlank(message = "Product ID is required")
    private String productId;
}
