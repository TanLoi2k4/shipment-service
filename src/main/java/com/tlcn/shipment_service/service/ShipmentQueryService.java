package com.tlcn.shipment_service.service;

import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.dto.ShipmentTrackingResponse;
import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.exception.ForbiddenException;
import com.tlcn.shipment_service.exception.ResourceNotFoundException;
import com.tlcn.shipment_service.model.Shipment;
import com.tlcn.shipment_service.model.ShipmentTracking;
import com.tlcn.shipment_service.repository.ShipmentRepository;
import com.tlcn.shipment_service.repository.ShipmentTrackingRepository;
import com.tlcn.shipment_service.util.CarrierCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipmentQueryService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;

    public List<ShipmentResponse> getAllShipments() {
        return shipmentRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ShipmentResponse> getShipmentsByVendor(String vendorKeycloakId) {
        return shipmentRepository.findByVendorKeycloakId(vendorKeycloakId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ShipmentResponse> getShipmentsByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
        return toResponse(shipment);
    }

    public ShipmentResponse getShipmentByIdForVendor(Long id, String vendorKeycloakId) {
        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + id));
        if (shipment.getVendorKeycloakId() == null || !shipment.getVendorKeycloakId().equals(vendorKeycloakId)) {
            throw new ForbiddenException("Access denied for shipment: " + id);
        }
        return toResponse(shipment);
    }

    public List<ShipmentTrackingResponse> getShipmentTracking(Long shipmentId) {
        return shipmentTrackingRepository.findByShipment_IdOrderByCreatedAtDesc(shipmentId).stream()
            .map(this::toTrackingResponse)
            .collect(Collectors.toList());
    }

    public List<ShipmentTrackingResponse> getShipmentTrackingForVendor(Long shipmentId, String vendorKeycloakId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentId));
        if (shipment.getVendorKeycloakId() == null || !shipment.getVendorKeycloakId().equals(vendorKeycloakId)) {
            throw new ForbiddenException("Access denied for shipment tracking: " + shipmentId);
        }
        return shipmentTrackingRepository.findByShipment_IdOrderByCreatedAtDesc(shipmentId).stream()
            .map(this::toTrackingResponse)
            .collect(Collectors.toList());
    }

    public ShipmentResponse toResponse(Shipment shipment) {
        return ShipmentResponse.builder()
            .id(shipment.getId())
            .subOrderId(shipment.getSubOrderId())
            .orderId(shipment.getOrderId())
            .shipmentType(shipment.getShipmentType() == null ? ShipmentType.OUTBOUND : shipment.getShipmentType())
            .returnRequestId(shipment.getReturnRequestId())
            .pickupAddress(shipment.getPickupAddress())
            .deliveryAddress(shipment.getDeliveryAddress())
            .externalShipmentId(shipment.getExternalShipmentId())
            .carrierCode(shipment.getCarrierCode())
            .carrierName(resolveCarrierName(shipment.getCarrierCode()))
            .carrierTrackingId(shipment.getExternalShipmentId())
            .serviceLevel(shipment.getServiceLevel())
            .status(shipment.getStatus())
            .createdAt(shipment.getCreatedAt())
            .updatedAt(shipment.getUpdatedAt())
            .mocked(shipment.getMocked())
            .build();
    }

    public Optional<Shipment> findOutboundBySubOrderId(Long subOrderId) {
        return shipmentRepository.findFirstBySubOrderIdAndShipmentTypeOrderByIdAsc(subOrderId, ShipmentType.OUTBOUND)
            .or(() -> shipmentRepository.findFirstBySubOrderIdAndShipmentTypeIsNullOrderByIdAsc(subOrderId));
    }

    public Optional<Shipment> findReturnBySubOrderId(Long subOrderId) {
        return shipmentRepository.findFirstBySubOrderIdAndShipmentTypeOrderByIdAsc(subOrderId, ShipmentType.RETURN);
    }

    public Optional<Shipment> findBySubOrderIdAndType(Long subOrderId, ShipmentType type) {
        if (type == ShipmentType.RETURN) {
            return findReturnBySubOrderId(subOrderId);
        }
        return findOutboundBySubOrderId(subOrderId);
    }

    private String resolveCarrierName(String carrierCode) {
        if (carrierCode == null || carrierCode.isBlank()) {
            return null;
        }
        return CarrierCatalog.displayName(carrierCode);
    }

    private ShipmentTrackingResponse toTrackingResponse(ShipmentTracking tracking) {
        return ShipmentTrackingResponse.builder()
            .id(tracking.getId())
            .trackingId(tracking.getTrackingId())
            .location(tracking.getLocation())
            .eta(tracking.getEta())
            .driverName(tracking.getDriverName())
            .driverPhone(tracking.getDriverPhone())
            .vehicle(tracking.getVehicle())
            .rawCarrierData(tracking.getRawCarrierData())
            .createdAt(tracking.getCreatedAt())
            .updatedAt(tracking.getUpdatedAt())
            .build();
    }
}
