package com.huertos.parcelservice.controller;

import com.huertos.parcelservice.dto.LocationDTO;
import com.huertos.parcelservice.dto.ParcelRequest;
import com.huertos.parcelservice.dto.ParcelResponse;
import com.huertos.parcelservice.model.SoilType;
import com.huertos.parcelservice.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParcelController.class)
class ParcelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelService parcelService;

    @Test
    void createsParcelAndReturnsCreatedResource() throws Exception {
        when(parcelService.create(any(ParcelRequest.class))).thenReturn(sampleParcel());

        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Parcela Norte  ",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "limoso",
                                  "ownerOrLead": "  Equipo del huerto  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Parcela Norte"))
                .andExpect(jsonPath("$.location.latitude").value(19.4326))
                .andExpect(jsonPath("$.location.longitude").value(-99.1332))
                .andExpect(jsonPath("$.soilType").value("limoso"))
                .andExpect(jsonPath("$.ownerOrLead").value("Equipo del huerto"))
                .andExpect(jsonPath("$.createdAt").exists());

        ArgumentCaptor<ParcelRequest> requestCaptor = ArgumentCaptor.forClass(ParcelRequest.class);
        verify(parcelService).create(requestCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("Parcela Norte", requestCaptor.getValue().name());
        org.junit.jupiter.api.Assertions.assertEquals("Equipo del huerto", requestCaptor.getValue().ownerOrLead());
    }

    @Test
    void rejectsMissingFieldsInvalidCoordinatesAndControlCharacters() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Parcela\\nNorte",
                                  "location": {"latitude": 91, "longitude": -181},
                                  "soilType": "arenoso",
                                  "ownerOrLead": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/parcels"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details['location.latitude']").exists())
                .andExpect(jsonPath("$.details['location.longitude']").exists())
                .andExpect(jsonPath("$.details.ownerOrLead").exists());
    }

    @Test
    void rejectsBlankAndOverlongNames() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "limoso",
                                  "ownerOrLead": "Equipo"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());

        String overlongName = "N".repeat(121);
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "limoso",
                                  "ownerOrLead": "Equipo"
                                }
                                """.formatted(overlongName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    void returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "El cuerpo JSON, los parámetros o la ruta no tienen un formato válido"))
                .andExpect(jsonPath("$.details").isMap());
    }

    @Test
    void rejectsUnsupportedSoilType() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Parcela Norte",
                                  "location": {"latitude": 19.4326, "longitude": -99.1332},
                                  "soilType": "turboso",
                                  "ownerOrLead": "Equipo"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/parcels"));
    }

    @Test
    void listsParcels() throws Exception {
        when(parcelService.findAll()).thenReturn(List.of(sampleParcel()));

        mockMvc.perform(get("/api/v1/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].soilType").value("limoso"));
    }

    @Test
    void returnsParcelById() throws Exception {
        when(parcelService.findById(7L)).thenReturn(sampleParcel());

        mockMvc.perform(get("/api/v1/parcels/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.location.latitude").value(19.4326));
    }

    @Test
    void returnsUniformNotFoundResponseForUnknownId() throws Exception {
        when(parcelService.findById(404L))
                .thenThrow(new com.huertos.parcelservice.exception.ResourceNotFoundException("Parcela", 404L));

        mockMvc.perform(get("/api/v1/parcels/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Parcela con id 404 no existe"))
                .andExpect(jsonPath("$.path").value("/api/v1/parcels/404"));
    }

    private static ParcelResponse sampleParcel() {
        return new ParcelResponse(
                7L,
                "Parcela Norte",
                new LocationDTO(new BigDecimal("19.4326"), new BigDecimal("-99.1332")),
                SoilType.LIMOSO,
                "Equipo del huerto",
                Instant.parse("2026-10-14T12:00:00Z")
        );
    }
}
