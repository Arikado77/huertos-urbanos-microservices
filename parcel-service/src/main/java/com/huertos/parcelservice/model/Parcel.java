package com.huertos.parcelservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "parcels")
public class Parcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SoilType soilType;

    @Column(nullable = false, length = 160)
    private String ownerOrLead;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Parcel() {
    }

    public Parcel(
            String name,
            BigDecimal latitude,
            BigDecimal longitude,
            SoilType soilType,
            String ownerOrLead
    ) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.soilType = soilType;
        this.ownerOrLead = ownerOrLead;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public SoilType getSoilType() {
        return soilType;
    }

    public String getOwnerOrLead() {
        return ownerOrLead;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
