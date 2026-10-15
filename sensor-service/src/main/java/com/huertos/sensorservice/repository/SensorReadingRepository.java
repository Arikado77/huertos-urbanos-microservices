package com.huertos.sensorservice.repository;

import com.huertos.sensorservice.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findAllByParcelIdOrderByRecordedAtDescIdDesc(Long parcelId);
}
