package com.huertos.parcelservice.dto;

import com.huertos.parcelservice.model.SoilType;

import java.time.Instant;

public record ParcelResponse(
        Long id,
        String name,
        LocationDTO location,
        SoilType soilType,
        String ownerOrLead,
        Instant createdAt
) {
}
