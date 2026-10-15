package com.huertos.parcelservice.repository;

import com.huertos.parcelservice.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}
