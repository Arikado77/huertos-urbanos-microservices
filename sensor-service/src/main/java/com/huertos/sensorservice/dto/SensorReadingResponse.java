package com.huertos.sensorservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SensorReadingResponse(
        Long id,
        Long parcelId,
        BigDecimal soilMoisture,
        BigDecimal temperature,
        BigDecimal ph,
        BigDecimal nitrogen,
        BigDecimal phosphorus,
        BigDecimal potassium,
        BigDecimal airQuality,
        Instant recordedAt
) {
}
