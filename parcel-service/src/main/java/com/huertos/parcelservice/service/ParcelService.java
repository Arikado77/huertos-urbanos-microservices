package com.huertos.parcelservice.service;

import com.huertos.parcelservice.dto.LocationDTO;
import com.huertos.parcelservice.dto.ParcelRequest;
import com.huertos.parcelservice.dto.ParcelResponse;
import com.huertos.parcelservice.exception.ResourceNotFoundException;
import com.huertos.parcelservice.model.Parcel;
import com.huertos.parcelservice.repository.ParcelRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ParcelService {
    private final ParcelRepository parcelRepository;

    public ParcelService(ParcelRepository parcelRepository) {
        this.parcelRepository = parcelRepository;
    }

    public ParcelResponse create(ParcelRequest request) {
        Parcel parcel = new Parcel(
                request.name(),
                request.location().latitude(),
                request.location().longitude(),
                request.soilType(),
                request.ownerOrLead()
        );
        return toResponse(parcelRepository.save(parcel));
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> findAll() {
        return parcelRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ParcelService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParcelResponse findById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcela", id));
        return toResponse(parcel);
    }

    private static ParcelResponse toResponse(Parcel parcel) {
        return new ParcelResponse(
                parcel.getId(),
                parcel.getName(),
                new LocationDTO(parcel.getLatitude(), parcel.getLongitude()),
                parcel.getSoilType(),
                parcel.getOwnerOrLead(),
                parcel.getCreatedAt()
        );
    }
}
