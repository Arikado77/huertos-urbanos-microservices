package com.huertos.sensorservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long parcelId;

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal soilMoisture;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal ph;

    @Column(nullable = false, precision = 9, scale = 3)
    private BigDecimal nitrogen;

    @Column(nullable = false, precision = 9, scale = 3)
    private BigDecimal phosphorus;

    @Column(nullable = false, precision = 9, scale = 3)
    private BigDecimal potassium;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal airQuality;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    protected SensorReading() {
    }

    public SensorReading(
            Long parcelId,
            BigDecimal soilMoisture,
            BigDecimal temperature,
            BigDecimal ph,
            BigDecimal nitrogen,
            BigDecimal phosphorus,
            BigDecimal potassium,
            BigDecimal airQuality
    ) {
        this.parcelId = parcelId;
        this.soilMoisture = soilMoisture;
        this.temperature = temperature;
        this.ph = ph;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.airQuality = airQuality;
    }

    @PrePersist
    void setRecordedAt() {
        if (recordedAt == null) {
            recordedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getParcelId() {
        return parcelId;
    }

    public BigDecimal getSoilMoisture() {
        return soilMoisture;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public BigDecimal getPh() {
        return ph;
    }

    public BigDecimal getNitrogen() {
        return nitrogen;
    }

    public BigDecimal getPhosphorus() {
        return phosphorus;
    }

    public BigDecimal getPotassium() {
        return potassium;
    }

    public BigDecimal getAirQuality() {
        return airQuality;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
