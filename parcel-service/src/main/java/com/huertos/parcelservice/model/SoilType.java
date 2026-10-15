package com.huertos.parcelservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum SoilType {
    ARCILLOSO,
    LIMOSO,
    ARENOSO;

    @JsonCreator
    public static SoilType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El tipo de suelo es obligatorio");
        }
        return SoilType.valueOf(value.strip().toUpperCase(Locale.ROOT));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
