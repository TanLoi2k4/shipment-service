package com.tlcn.shipment_service.service;

import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.dto.ShipmentTrackingResponse;
import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.exception.ForbiddenException;
import com.tlcn.shipment_service.exception.ResourceNotFoundException;
import com.tlcn.shipment_service.model.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentVendorService {

    private final ShipmentQueryService shipmentQueryService;

    public List<ShipmentResponse> getVendorShipments(String vendorKeycloakId) {
        return shipmentQueryService.getShipmentsByVendor(vendorKeycloakId);
    }

    public ShipmentResponse getVendorShipmentById(Long id, String vendorKeycloakId) {
        return shipmentQueryService.getShipmentByIdForVendor(id, vendorKeycloakId);
    }

    public ShipmentResponse getVendorShipmentBySubOrder(Long subOrderId, String vendorKeycloakId, ShipmentType type) {
        Shipment shipment = shipmentQueryService.findBySubOrderIdAndType(
                subOrderId,
                type == null ? ShipmentType.OUTBOUND : type
            )
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for subOrderId: " + subOrderId));
        assertVendorOwnsShipment(shipment, vendorKeycloakId, "subOrderId: " + subOrderId);
        return shipmentQueryService.toResponse(shipment);
    }

    public List<ShipmentTrackingResponse> getVendorShipmentTracking(Long shipmentId, String vendorKeycloakId) {
        return shipmentQueryService.getShipmentTrackingForVendor(shipmentId, vendorKeycloakId);
    }

    private void assertVendorOwnsShipment(Shipment shipment, String vendorKeycloakId, String context) {
        if (shipment.getVendorKeycloakId() == null || !shipment.getVendorKeycloakId().equals(vendorKeycloakId)) {
            throw new ForbiddenException("Access denied for " + context);
        }
    }
}
