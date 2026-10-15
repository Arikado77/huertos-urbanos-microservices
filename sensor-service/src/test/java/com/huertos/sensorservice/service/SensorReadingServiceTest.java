package com.huertos.sensorservice.service;

import com.huertos.sensorservice.client.ParcelServiceClient;
import com.huertos.sensorservice.dto.SensorReadingRequest;
import com.huertos.sensorservice.exception.ResourceNotFoundException;
import com.huertos.sensorservice.exception.UpstreamServiceException;
import com.huertos.sensorservice.model.SensorReading;
import com.huertos.sensorservice.repository.SensorReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {
    @Mock
    private SensorReadingRepository repository;
    @Mock
    private ParcelServiceClient parcelServiceClient;

    private SensorReadingService service;

    @BeforeEach
    void setUp() {
        service = new SensorReadingService(repository, parcelServiceClient);
    }

    @Test
    void verifiesParcelBeforeSavingReading() {
        when(repository.save(any(SensorReading.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(validRequest());

        verify(parcelServiceClient).assertParcelExists(7L);
        verify(repository).save(any(SensorReading.class));
    }

    @Test
    void doesNotSaveWhenParcelDoesNotExist() {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Parcela", 999L))
                .when(parcelServiceClient).assertParcelExists(999L);

        assertThrows(ResourceNotFoundException.class, () -> service.create(requestForParcel(999L)));
        verify(repository, never()).save(any(SensorReading.class));
    }

    @Test
    void propagatesCommunicationFailureWithoutSaving() {
        org.mockito.Mockito.doThrow(new UpstreamServiceException("parcel-service unavailable",
                        new RuntimeException("connection refused")))
                .when(parcelServiceClient).assertParcelExists(7L);

        assertThrows(UpstreamServiceException.class, () -> service.create(validRequest()));
        verify(repository, never()).save(any(SensorReading.class));
    }

    @Test
    void raisesNotFoundForMissingReadingId() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(404L));
    }

    private static SensorReadingRequest validRequest() {
        return requestForParcel(7L);
    }

    private static SensorReadingRequest requestForParcel(Long parcelId) {
        return new SensorReadingRequest(
                parcelId,
                new BigDecimal("42.5"),
                new BigDecimal("21.5"),
                new BigDecimal("6.5"),
                new BigDecimal("12"),
                new BigDecimal("8"),
                new BigDecimal("10"),
                new BigDecimal("40")
        );
    }
}
