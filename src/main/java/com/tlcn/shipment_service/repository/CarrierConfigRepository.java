package com.tlcn.shipment_service.repository;

import com.tlcn.shipment_service.model.CarrierConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrierConfigRepository extends JpaRepository<CarrierConfig, Long> {
    Optional<CarrierConfig> findByCarrierCodeIgnoreCaseAndActiveTrue(String carrierCode);

    Optional<CarrierConfig> findByCarrierCodeIgnoreCase(String carrierCode);

    Optional<CarrierConfig> findFirstByActiveTrueOrderByIdAsc();

    List<CarrierConfig> findByActiveTrueOrderByIdAsc();
}
