package com.huertos.sensorservice.controller;

import com.huertos.sensorservice.dto.SensorReadingRequest;
import com.huertos.sensorservice.dto.SensorReadingResponse;
import com.huertos.sensorservice.exception.ResourceNotFoundException;
import com.huertos.sensorservice.exception.UpstreamServiceException;
import com.huertos.sensorservice.service.SensorReadingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorReadingController.class)
class SensorReadingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorReadingService service;

    @Test
    void createsReadingAndReturnsCreatedResponse() throws Exception {
        when(service.create(any(SensorReadingRequest.class))).thenReturn(sampleReading());

        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.parcelId").value(7))
                .andExpect(jsonPath("$.soilMoisture").value(42.5))
                .andExpect(jsonPath("$.ph").value(6.5))
                .andExpect(jsonPath("$.recordedAt").exists());

        verify(service).create(any(SensorReadingRequest.class));
    }

    @Test
    void rejectsPhAboveFourteenAndSoilMoistureOutsidePercentageRange() throws Exception {
        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parcelId": 7, "soilMoisture": 101, "temperature": 21.5,
                                  "ph": 14.1, "nitrogen": 12, "phosphorus": 8,
                                  "potassium": 10, "airQuality": 40
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.soilMoisture").exists())
                .andExpect(jsonPath("$.details.ph").exists());
    }

    @Test
    void rejectsInvalidEnvironmentalMeasurements() throws Exception {
        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parcelId": 7, "soilMoisture": 20, "temperature": 81,
                                  "ph": 6, "nitrogen": -1, "phosphorus": 8,
                                  "potassium": 10, "airQuality": 5001
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.temperature").exists())
                .andExpect(jsonPath("$.details.nitrogen").exists())
                .andExpect(jsonPath("$.details.airQuality").exists());
    }

    @Test
    void rejectsMissingRequiredFieldsAndNonPositiveParcelId() throws Exception {
        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parcelId": 0, "soilMoisture": 20,
                                  "temperature": 21.5, "ph": 6.5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.parcelId").exists())
                .andExpect(jsonPath("$.details.nitrogen").exists())
                .andExpect(jsonPath("$.details.airQuality").exists());
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/sensors/readings"));
    }

    @Test
    void listsReadings() throws Exception {
        when(service.findAll()).thenReturn(List.of(sampleReading()));

        mockMvc.perform(get("/api/v1/sensors/readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15));
    }

    @Test
    void retrievesReadingById() throws Exception {
        when(service.findById(15L)).thenReturn(sampleReading());

        mockMvc.perform(get("/api/v1/sensors/readings/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelId").value(7));
    }

    @Test
    void retrievesReadingsByParcel() throws Exception {
        when(service.findByParcelId(7L)).thenReturn(List.of(sampleReading()));

        mockMvc.perform(get("/api/v1/sensors/readings/parcel/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parcelId").value(7));
    }

    @Test
    void returnsNotFoundForUnknownReadingAndParcel() throws Exception {
        when(service.findById(999L)).thenThrow(new ResourceNotFoundException("Lectura", 999L));
        when(service.findByParcelId(999L)).thenThrow(new ResourceNotFoundException("Parcela", 999L));

        mockMvc.perform(get("/api/v1/sensors/readings/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Lectura con id 999 no existe"));

        mockMvc.perform(get("/api/v1/sensors/readings/parcel/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Parcela con id 999 no existe"));
    }

    @Test
    void reportsParcelServiceUnavailable() throws Exception {
        when(service.create(any(SensorReadingRequest.class)))
                .thenThrow(new UpstreamServiceException("No fue posible comunicarse con parcel-service",
                        new RuntimeException("connection refused")));

        mockMvc.perform(post("/api/v1/sensors/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("No fue posible comunicarse con parcel-service"));
    }

    private static String validPayload() {
        return """
                {
                  "parcelId": 7, "soilMoisture": 42.5, "temperature": 21.5,
                  "ph": 6.5, "nitrogen": 12, "phosphorus": 8,
                  "potassium": 10, "airQuality": 40
                }
                """;
    }

    private static SensorReadingResponse sampleReading() {
        return new SensorReadingResponse(
                15L,
                7L,
                new BigDecimal("42.500"),
                new BigDecimal("21.50"),
                new BigDecimal("6.50"),
                new BigDecimal("12.000"),
                new BigDecimal("8.000"),
                new BigDecimal("10.000"),
                new BigDecimal("40.00"),
                Instant.parse("2026-10-14T21:00:00Z")
        );
    }
}
