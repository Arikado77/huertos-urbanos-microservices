package com.huertos.sensorservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SensorReadingRequest(
        @NotNull
        @Positive
        Long parcelId,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100.0")
        @Digits(integer = 3, fraction = 3)
        BigDecimal soilMoisture,

        @NotNull
        @DecimalMin("-50.0")
        @DecimalMax("80.0")
        @Digits(integer = 2, fraction = 2)
        BigDecimal temperature,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("14.0")
        @Digits(integer = 2, fraction = 2)
        BigDecimal ph,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100000.0")
        @Digits(integer = 6, fraction = 3)
        BigDecimal nitrogen,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100000.0")
        @Digits(integer = 6, fraction = 3)
        BigDecimal phosphorus,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("100000.0")
        @Digits(integer = 6, fraction = 3)
        BigDecimal potassium,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("5000.0")
        @Digits(integer = 4, fraction = 2)
        BigDecimal airQuality
) {
}
