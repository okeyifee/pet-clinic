package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.ShoppingBasket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ShoppingBasketRepository extends JpaRepository<ShoppingBasket, String> {

    @Query("""
        SELECT b FROM ShoppingBasket b
        JOIN b.customer c
        WHERE b.id = :basketId 
        AND c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    Optional<ShoppingBasket> findByIdWithAccess(
            @Param("basketId") String basketId,
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT b FROM ShoppingBasket b
        JOIN b.customer c
        WHERE c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    Page<ShoppingBasket> findByCustomerWithAccess(
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM ShoppingBasket b
        JOIN b.customer c
        WHERE b.id IN :basketIds
        AND c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    List<ShoppingBasket> findByCustomerIdAndBasketIds(
            @Param("customerId") String customerId,
            @Param("basketIds") List<String> basketIds,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    /**
     * Stream all baskets for a specific customer with access control.
     * IMPORTANT: This stream must be closed after use to prevent connection leaks.
     */
    @Query("""
        SELECT b FROM ShoppingBasket b
        JOIN b.customer c
        WHERE c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    Stream<ShoppingBasket> streamAllWithAccess(
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );
}