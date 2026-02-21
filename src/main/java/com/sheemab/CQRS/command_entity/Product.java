package com.sheemab.CQRS.command_entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WRITE MODEL — the domain aggregate on the command side.
 *
 * Key CQRS rule:
 *   This entity is ONLY used by the command side (create/update/delete).
 *   The query side has its own read model (ProductView) which is
 *   optimized for reading and shaped for what the UI needs.
 *
 *   They are kept separate intentionally:
 *   - Write model is normalized, has business rules, domain logic
 *   - Read model is denormalized, flat, optimized for fast queries
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;   // optimistic locking

    // ── Domain behaviour ──────────────────────────────────────────────────────
    // Business rules live HERE, not in the service layer

    public void update(String name, String category,
                       BigDecimal price, Integer stock, String description) {
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.stock       = stock;
        this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isInStock() {
        return this.stock > 0;
    }
}