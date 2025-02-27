package com.samuel.sniffers.repository.view;

import com.samuel.sniffers.entity.view.CustomerBasketItemView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerBasketItemViewRepository extends JpaRepository<CustomerBasketItemView, String> {
    @Query(value = "SELECT * FROM customer_basket_item_overview WHERE owner_token = :token OR :isAdmin = true",
            nativeQuery = true)
    List<CustomerBasketItemView> findAllWithAccess(
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );

    @Query(value = "SELECT * FROM customer_basket_item_overview WHERE customer_name = :customerName AND (owner_token = :token OR :isAdmin = true)",
            nativeQuery = true)
    List<CustomerBasketItemView> findByCustomerNameWithAccess(
            @Param("customerName") String customerName,
            @Param("token") String token,
            @Param("isAdmin") boolean isAdmin
    );
}
