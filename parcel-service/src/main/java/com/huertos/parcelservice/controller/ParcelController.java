package com.huertos.parcelservice.controller;

import com.huertos.parcelservice.dto.ParcelRequest;
import com.huertos.parcelservice.dto.ParcelResponse;
import com.huertos.parcelservice.service.ParcelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
public class ParcelController {
    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping
    public ResponseEntity<ParcelResponse> create(@Valid @RequestBody ParcelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parcelService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ParcelResponse>> findAll() {
        return ResponseEntity.ok(parcelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelService.findById(id));
    }
}
