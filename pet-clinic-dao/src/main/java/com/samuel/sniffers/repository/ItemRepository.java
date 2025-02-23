package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String> {


    @Query("""
        SELECT i FROM Item i
        JOIN i.basket b
        JOIN b.customer c
        WHERE i.id = :itemId
        AND b.id = :basketId
        AND c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    Optional<Item> findByIdWithAccess(
            @Param("itemId") String itemId,
            @Param("basketId") String basketId,
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT i FROM Item i
        JOIN i.basket b
        JOIN b.customer c
        WHERE b.id = :basketId
        AND c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    List<Item> findByCustomerWithAccess(
            @Param("basketId") String basketId,
            @Param("customerId") String customerId,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT i FROM Item i
        JOIN i.basket b
        JOIN b.customer c
        WHERE i.id IN :itemIds
        AND b.id = :basketId
        AND c.id = :customerId
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    List<Item> findByCustomerIdAndBasketIds(
            @Param("customerId") String customerId,
            @Param("basketId") String basketId,
            @Param("itemIds") List<String> itemIds,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );
}
