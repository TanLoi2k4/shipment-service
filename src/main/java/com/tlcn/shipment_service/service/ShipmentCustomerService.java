package com.tlcn.shipment_service.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tlcn.shipment_service.client.OrderServiceClient;
import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.dto.ShipmentTrackingResponse;
import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.exception.ForbiddenException;
import com.tlcn.shipment_service.exception.ResourceNotFoundException;
import com.tlcn.shipment_service.model.Shipment;
import com.tlcn.shipment_service.repository.ShipmentRepository;
import com.tlcn.shipment_service.repository.ShipmentTrackingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentCustomerService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final ShipmentQueryService shipmentQueryService;
    private final OrderServiceClient orderServiceClient;

    public List<ShipmentResponse> getCustomerShipments(String authorizationHeader) {
        Set<Long> ownedSubOrderIds = orderServiceClient.getCustomerSubOrderIds(authorizationHeader);
        if (ownedSubOrderIds.isEmpty()) {
            return List.of();
        }
        return shipmentRepository.findBySubOrderIdIn(ownedSubOrderIds).stream()
            .map(shipmentQueryService::toResponse)
            .collect(Collectors.toList());
    }

    public ShipmentResponse getCustomerShipmentBySubOrder(Long subOrderId, String authorizationHeader, ShipmentType type) {
        assertCustomerOwnsSubOrder(subOrderId, authorizationHeader);
        Shipment shipment = shipmentQueryService.findBySubOrderIdAndType(
                subOrderId,
                type == null ? ShipmentType.OUTBOUND : type
            )
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for subOrderId: " + subOrderId));
        return shipmentQueryService.toResponse(shipment);
    }

    public ShipmentResponse getCustomerShipmentById(Long shipmentId, String authorizationHeader) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentId));
        assertCustomerOwnsSubOrder(shipment.getSubOrderId(), authorizationHeader);
        return shipmentQueryService.toResponse(shipment);
    }

    public List<ShipmentTrackingResponse> getCustomerShipmentTracking(Long shipmentId, String authorizationHeader) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found: " + shipmentId));
        assertCustomerOwnsSubOrder(shipment.getSubOrderId(), authorizationHeader);
        return shipmentQueryService.getShipmentTracking(shipmentId);
    }

    private void assertCustomerOwnsSubOrder(Long subOrderId, String authorizationHeader) {
        Set<Long> ownedSubOrderIds = orderServiceClient.getCustomerSubOrderIds(authorizationHeader);
        if (!ownedSubOrderIds.contains(subOrderId)) {
            throw new ForbiddenException("Access denied for subOrderId: " + subOrderId);
        }
    }
}
