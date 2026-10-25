package com.tlcn.shipment_service.repository;

import com.tlcn.shipment_service.model.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {
    List<ShipmentTracking> findByShipment_IdOrderByCreatedAtDesc(Long shipmentId);

    boolean existsByShipment_IdAndTrackingId(Long shipmentId, String trackingId);
}
