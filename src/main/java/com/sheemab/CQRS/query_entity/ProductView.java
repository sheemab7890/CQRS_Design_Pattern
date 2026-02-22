package com.sheemab.CQRS.query_entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * READ MODEL — the query side's own view of a product.
 *
 * Key CQRS rules demonstrated here:
 *
 *   1. This is a SEPARATE table (product_views) from the write model (products).
 *      The query side NEVER touches the products table directly.
 *
 *   2. This table is denormalized and shaped for what the UI/client needs.
 *      For example: priceFormatted is pre-computed here so the client
 *      doesn't have to do it. In a real system you'd add whatever fields
 *      make queries fast and simple.
 *
 *   3. This entity has NO business logic, NO domain methods.
 *      It is purely a data container for reading.
 *
 *   4. It is kept in sync by the ProjectionEventListener which listens
 *      to domain events published by the write side.
 */
@Entity
@Table(
        name = "product_views",
        indexes = {
                @Index(name = "idx_pv_category",   columnList = "category"),
                @Index(name = "idx_pv_active",     columnList = "active"),
                @Index(name = "idx_pv_price",      columnList = "price")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductView {

    @Id
    private String id;          // same ID as the write-side Product

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // ── Pre-computed/denormalized fields ──────────────────────────────────────
    // These make queries cheaper — the client gets a ready-to-display value

    @Column(nullable = false)
    private String stockStatus;         // "IN STOCK" / "OUT OF STOCK"

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;
}
