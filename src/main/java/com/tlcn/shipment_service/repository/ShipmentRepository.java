package com.tlcn.shipment_service.repository;

import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findFirstBySubOrderIdAndShipmentTypeOrderByIdAsc(Long subOrderId, ShipmentType shipmentType);

    Optional<Shipment> findFirstBySubOrderIdAndShipmentTypeIsNullOrderByIdAsc(Long subOrderId);

    Optional<Shipment> findByReturnRequestId(Long returnRequestId);

    Optional<Shipment> findByExternalShipmentId(String externalShipmentId);

    List<Shipment> findByVendorKeycloakId(String vendorKeycloakId);

    List<Shipment> findByOrderId(Long orderId);

    List<Shipment> findBySubOrderIdIn(Set<Long> subOrderIds);
}
