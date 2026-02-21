package com.sheemab.CQRS.repository;


import com.sheemab.CQRS.command_entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * WRITE REPOSITORY — exclusively used by the command side.
 *
 * Key CQRS rule:
 *   The query side must NEVER use this repository.
 *   It has its own ProductReadRepository against the ProductView table.
 *   This strict separation means each side can evolve independently.
 */
@Repository
public interface ProductWriteRepository extends JpaRepository<Product, String> {

    boolean existsByNameAndCategory(String name, String category);
}
