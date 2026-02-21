package com.sheemab.CQRS.command;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * COMMAND — represents the INTENT to create a product.
 *
 * Key CQRS rule: Commands are imperative ("do this").
 * They carry everything the handler needs and NOTHING more.
 * They do NOT return domain data — only a success/failure signal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductCommand {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Positive(message = "Stock must be greater than zero")
    private Integer stock;

    private String description;
}
