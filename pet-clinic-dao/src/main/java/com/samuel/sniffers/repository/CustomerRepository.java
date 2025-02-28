package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Query("""
        SELECT c FROM Customer c 
        WHERE c.ownerToken = :token OR :isAdmin = true
    """)
    List<Customer> findAllWithAccess(@Param("token") String token, @Param("isAdmin") boolean isAdmin);

    @Query("""
        SELECT c FROM Customer c 
        LEFT JOIN FETCH c.baskets b
        LEFT JOIN FETCH b.items i
        WHERE c.id = :id AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    Optional<Customer> findByIdWithAccess(
            @Param("id") String id,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT c FROM Customer c 
        WHERE c.id IN :ids 
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    List<Customer> findAllByIdAndOwnerToken(
            @Param("ids") List<String> ids,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Customer c
        LEFT JOIN c.baskets b
        LEFT JOIN b.items i
        WHERE c.name = :name 
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    boolean existByNameAndOwnerToken(
            @Param("name") String name,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Customer c
        LEFT JOIN c.baskets b
        LEFT JOIN b.items i
        WHERE c.id = :id 
        AND (c.ownerToken = :token OR :isAdmin = true)
    """)
    boolean existByIdAndOwnerToken(
            @Param("id") String id,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    /**
     * Stream all customers with access control based on token and admin status.
     * IMPORTANT: This stream must be closed after use to prevent connection leaks.
     */
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.ownerToken = :token OR :isAdmin = true
    """)
    Stream<Customer> streamAllWithAccess(@Param("token") String token, @Param("isAdmin") boolean isAdmin);

    // Repository method
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.ownerToken = :token OR :isAdmin = true
    """)
    Page<Customer> findAllWithAccess(
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin,
            Pageable pageable
    );
}
