package com.samuel.sniffers.entity.view;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.samuel.sniffers.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Immutable
@Table(name = "customer_basket_item_overview")
@IdClass(CustomerBasketItemView.CompositeKey.class)
public class CustomerBasketItemView {

    @Id
    @Column(name = "customer_id", columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_timezone")
    private String customerTimezone;

    @Column(name = "owner_token")
    private String ownerToken;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "customer_created")
    private LocalDateTime customerCreated;

    @Column(name = "basket_id", columnDefinition = "CHAR(36)")
    private String basketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "basket_status")
    private BasketStatus basketStatus;

    @Column(name = "basket_created")
    private LocalDateTime basketCreated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "basket_status_date")
    private LocalDateTime basketStatusDate;

    @Column(name = "item_id", columnDefinition = "CHAR(36)")
    private String itemId;

    @Column(name = "item_description")
    private String itemDescription;

    @Column(name = "item_amount")
    private Integer itemAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "item_created")
    private LocalDateTime itemCreated;

    // Composite Key Class
    public static class CompositeKey implements Serializable {
        private String customerId;
        private String basketId;
        private String itemId;

        // Constructors
        public CompositeKey() {}

        public CompositeKey(String customerId, String basketId, String itemId) {
            this.customerId = customerId;
            this.basketId = basketId;
            this.itemId = itemId;
        }

        // Equals and HashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompositeKey that = (CompositeKey) o;
            return Objects.equals(customerId, that.customerId) &&
                    Objects.equals(basketId, that.basketId) &&
                    Objects.equals(itemId, that.itemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(customerId, basketId, itemId);
        }
    }
}
