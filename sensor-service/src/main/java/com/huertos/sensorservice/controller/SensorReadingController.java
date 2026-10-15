package com.huertos.sensorservice.controller;

import com.huertos.sensorservice.dto.SensorReadingRequest;
import com.huertos.sensorservice.dto.SensorReadingResponse;
import com.huertos.sensorservice.service.SensorReadingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/sensors/readings")
public class SensorReadingController {
    private final SensorReadingService service;

    public SensorReadingController(SensorReadingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SensorReadingResponse> create(@Valid @RequestBody SensorReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SensorReadingResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorReadingResponse> findById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<SensorReadingResponse>> findByParcelId(
            @PathVariable @Positive Long parcelId
    ) {
        return ResponseEntity.ok(service.findByParcelId(parcelId));
    }
}
