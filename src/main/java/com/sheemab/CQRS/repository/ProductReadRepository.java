package com.sheemab.CQRS.repository;


import com.sheemab.CQRS.query_entity.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * READ REPOSITORY — exclusively used by the query side.
 *
 * Key CQRS rule:
 *   This repo ONLY reads from product_views table.
 *   The write side (ProductWriteRepository) NEVER touches this table directly.
 *   The product_views table is populated and maintained ONLY by
 *   the ProjectionEventListener responding to domain events.
 */
@Repository
public interface ProductReadRepository extends JpaRepository<ProductView, String> {

    // Find single active product
    Optional<ProductView> findByIdAndActiveTrue(String id);

    // Find all active products (paginated)
    Page<ProductView> findByActiveTrue(Pageable pageable);

    // Filter by category
    List<ProductView> findByCategoryAndActiveTrue(String category);

    // Check if view exists (used by projection listener)
    boolean existsById(String id);
}
