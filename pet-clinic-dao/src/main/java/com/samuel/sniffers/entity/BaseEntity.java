package com.samuel.sniffers.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)") // UUID v5 stored as CHAR(36)
    private String id;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @PrePersist
    protected void onCreate() {
        // Use a namespace and a unique name to generate UUID v5
        final String namespace = "urn:my-namespace";
        final String name = "unique-id-" + System.nanoTime(); // Unique name for each entity instance
        this.id = UUID.nameUUIDFromBytes((namespace + name).getBytes(StandardCharsets.UTF_8)).toString();
        this.created = LocalDateTime.now(); // Set created timestamp
    }
}