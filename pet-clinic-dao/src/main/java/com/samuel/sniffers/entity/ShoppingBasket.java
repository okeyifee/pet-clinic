package com.samuel.sniffers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samuel.sniffers.enums.BasketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shopping_baskets")
@Getter
@Setter
public class ShoppingBasket extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BasketStatus status = BasketStatus.NEW;

    @Column(name = "status_date", nullable = false)
    private LocalDateTime statusDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Item> items = new HashSet<>();

    @PrePersist
    @PreUpdate
    protected void onStatusChange() {
        statusDate = LocalDateTime.now();
    }
}