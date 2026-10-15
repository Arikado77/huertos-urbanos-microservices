package com.huertos.sensorservice.service;

import com.huertos.sensorservice.client.ParcelServiceClient;
import com.huertos.sensorservice.dto.SensorReadingRequest;
import com.huertos.sensorservice.dto.SensorReadingResponse;
import com.huertos.sensorservice.exception.ResourceNotFoundException;
import com.huertos.sensorservice.model.SensorReading;
import com.huertos.sensorservice.repository.SensorReadingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SensorReadingService {
    private final SensorReadingRepository repository;
    private final ParcelServiceClient parcelServiceClient;

    public SensorReadingService(
            SensorReadingRepository repository,
            ParcelServiceClient parcelServiceClient
    ) {
        this.repository = repository;
        this.parcelServiceClient = parcelServiceClient;
    }

    public SensorReadingResponse create(SensorReadingRequest request) {
        parcelServiceClient.assertParcelExists(request.parcelId());
        SensorReading reading = new SensorReading(
                request.parcelId(),
                request.soilMoisture(),
                request.temperature(),
                request.ph(),
                request.nitrogen(),
                request.phosphorus(),
                request.potassium(),
                request.airQuality()
        );
        return toResponse(repository.save(reading));
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "recordedAt", "id"))
                .stream()
                .map(SensorReadingService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SensorReadingResponse findById(Long id) {
        return repository.findById(id)
                .map(SensorReadingService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura", id));
    }

    @Transactional(readOnly = true)
    public List<SensorReadingResponse> findByParcelId(Long parcelId) {
        parcelServiceClient.assertParcelExists(parcelId);
        return repository.findAllByParcelIdOrderByRecordedAtDescIdDesc(parcelId)
                .stream()
                .map(SensorReadingService::toResponse)
                .toList();
    }

    private static SensorReadingResponse toResponse(SensorReading reading) {
        return new SensorReadingResponse(
                reading.getId(),
                reading.getParcelId(),
                reading.getSoilMoisture(),
                reading.getTemperature(),
                reading.getPh(),
                reading.getNitrogen(),
                reading.getPhosphorus(),
                reading.getPotassium(),
                reading.getAirQuality(),
                reading.getRecordedAt()
        );
    }
}
