package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.ShoppingBasket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

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
    List<ShoppingBasket> findByCustomerWithAccess(
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
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
}