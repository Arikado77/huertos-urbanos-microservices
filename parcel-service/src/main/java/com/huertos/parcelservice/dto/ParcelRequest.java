package com.huertos.parcelservice.dto;

import com.huertos.parcelservice.model.SoilType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;

public record ParcelRequest(
        @NotBlank
        @Size(max = 120)
        @Pattern(regexp = "^[^\\p{Cc}\\p{Cf}]*$")
        String name,

        @NotNull
        @Valid
        LocationDTO location,

        @NotNull
        SoilType soilType,

        @NotBlank
        @Size(max = 160)
        @Pattern(regexp = "^[^\\p{Cc}\\p{Cf}]*$")
        String ownerOrLead
) {
    public ParcelRequest {
        name = normalize(name);
        ownerOrLead = normalize(ownerOrLead);
    }

    private static String normalize(String value) {
        return value == null ? null : Normalizer.normalize(value.strip(), Normalizer.Form.NFC);
    }
}
