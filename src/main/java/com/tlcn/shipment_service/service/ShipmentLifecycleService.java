package com.tlcn.shipment_service.service;

import com.tlcn.shipment_service.dto.CarrierWebhookRequest;
import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.dto.kafka.ShipmentCreated;
import com.tlcn.shipment_service.dto.kafka.ShipmentStatusChanged;
import com.tlcn.shipment_service.dto.kafka.SubOrderShippingRequested;
import com.tlcn.shipment_service.enums.ShipmentStatus;
import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.exception.CarrierTransientException;
import com.tlcn.shipment_service.exception.ResourceNotFoundException;
import com.tlcn.shipment_service.kafka.ShipmentEventProducer;
import com.tlcn.shipment_service.model.CarrierConfig;
import com.tlcn.shipment_service.model.Shipment;
import com.tlcn.shipment_service.model.ShipmentTracking;
import com.tlcn.shipment_service.repository.ShipmentRepository;
import com.tlcn.shipment_service.repository.ShipmentTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentLifecycleService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final CarrierIntegrationService carrierIntegrationService;
    private final ShipmentEventProducer shipmentEventProducer;
    private final ShipmentQueryService shipmentQueryService;

    @Transactional
    public ShipmentResponse handleShipmentRequest(SubOrderShippingRequested request) {
        ShipmentType shipmentType = request.isReturnShipment() ? ShipmentType.RETURN : ShipmentType.OUTBOUND;
        Shipment existing = findExisting(request, shipmentType);
        if (existing != null) {
            return shipmentQueryService.toResponse(existing);
        }

        String pickupAddress = blankToNull(request.getPickupAddress());
        String deliveryAddress = blankToNull(request.getDeliveryAddress());
        String carrierPreference = blankToNull(request.getCarrierPreference());

        if (shipmentType == ShipmentType.RETURN) {
            Shipment outbound = shipmentQueryService.findOutboundBySubOrderId(request.getSubOrderId()).orElse(null);
            if (deliveryAddress == null && outbound != null) {
                deliveryAddress = blankToNull(outbound.getPickupAddress());
            }
            if (carrierPreference == null && outbound != null) {
                carrierPreference = blankToNull(outbound.getCarrierCode());
            }
        }

        Optional<CarrierConfig> resolvedConfig = carrierIntegrationService.resolveCarrierConfig(carrierPreference);
        if (resolvedConfig.isEmpty() || (shipmentType == ShipmentType.RETURN && (pickupAddress == null || deliveryAddress == null))) {
            String requestedCode = carrierPreference == null ? "UNCONFIGURED" : carrierPreference.trim().toUpperCase();
            return persistBookingFailed(request, requestedCode, shipmentType, pickupAddress, deliveryAddress);
        }

        CarrierConfig carrierConfig = resolvedConfig.get();
        Shipment placeholder = Shipment.builder()
            .subOrderId(request.getSubOrderId())
            .shipmentType(shipmentType)
            .returnRequestId(request.getReturnRequestId())
            .orderId(request.getOrderId())
            .vendorKeycloakId(request.getVendorKeycloakId())
            .carrierCode(carrierConfig.getCarrierCode())
            .externalShipmentId("PENDING")
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .serviceLevel(request.getServiceLevel())
            .status(ShipmentStatus.CREATED)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        CarrierIntegrationService.CarrierBookingResult bookingResult = carrierIntegrationService.bookShipment(placeholder, request, carrierConfig);
        // If booking failed
        if (!bookingResult.isSuccess() || bookingResult.getExternalShipmentId() == null || bookingResult.getExternalShipmentId().isBlank()) {
            // transient error -> throw so Kafka consumer can retry according to retry policy
            if (!bookingResult.isSuccess() && bookingResult.isTransientError()) {
                throw new CarrierTransientException("Transient carrier error for subOrderId=" + request.getSubOrderId());
            }

            return persistBookingFailed(request, carrierConfig.getCarrierCode(), shipmentType, pickupAddress, deliveryAddress);
        }

        Instant now = Instant.now();

        Shipment shipment = Shipment.builder()
            .subOrderId(request.getSubOrderId())
            .shipmentType(shipmentType)
            .returnRequestId(request.getReturnRequestId())
            .orderId(request.getOrderId())
            .vendorKeycloakId(request.getVendorKeycloakId())
            .carrierCode(carrierConfig.getCarrierCode())
            .externalShipmentId(bookingResult.getExternalShipmentId())
            .pickupAddress(placeholder.getPickupAddress())
            .deliveryAddress(placeholder.getDeliveryAddress())
            .serviceLevel(placeholder.getServiceLevel())
            .status(ShipmentStatus.BOOKED)
            .bookedAt(now)
            .createdAt(now)
            .updatedAt(now)
            .mocked(false)
            .build();
        Shipment saved = shipmentRepository.save(shipment);

        ShipmentCreated createdEvent = ShipmentCreated.builder()
            .eventId(UUID.randomUUID().toString())
            .correlationId(request.getCorrelationId())
            .shipmentId(saved.getId())
            .subOrderId(saved.getSubOrderId())
            .shipmentType(saved.getShipmentType() != null ? saved.getShipmentType().name() : ShipmentType.OUTBOUND.name())
            .returnRequestId(saved.getReturnRequestId())
            .externalShipmentId(saved.getExternalShipmentId())
            .carrierCode(saved.getCarrierCode())
            .status(saved.getStatus().name())
            .createdAt(saved.getCreatedAt())
            .mocked(saved.getMocked())
            .build();
        shipmentEventProducer.sendShipmentCreated(createdEvent);

        return shipmentQueryService.toResponse(saved);
    }

    @Transactional
    public ShipmentResponse handleCarrierWebhook(CarrierWebhookRequest request) {
        Shipment shipment = shipmentRepository.findByExternalShipmentId(request.getExternalShipmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for externalShipmentId=" + request.getExternalShipmentId()));

        Instant now = Instant.now();
        String trackingId = request.getTrackingId() == null || request.getTrackingId().isBlank() ? UUID.randomUUID().toString() : request.getTrackingId();

        if (!shipment.getStatus().canTransitionTo(request.getStatus())) {
            return shipmentQueryService.toResponse(shipment);
        }

        if (shipmentTrackingRepository.existsByShipment_IdAndTrackingId(shipment.getId(), trackingId)) {
            shipment.setStatus(request.getStatus());
            shipment.setUpdatedAt(now);
            Shipment updated = shipmentRepository.save(shipment);
            return shipmentQueryService.toResponse(updated);
        }

        ShipmentTracking tracking = ShipmentTracking.builder()
            .shipment(shipment)
            .trackingId(trackingId)
            .location(request.getLocation())
            .eta(request.getEta())
            .driverName(request.getDriverName())
            .driverPhone(request.getDriverPhone())
            .vehicle(request.getVehicle())
            .rawCarrierData(request.getRawCarrierData())
            .createdAt(now)
            .updatedAt(now)
            .build();
        shipmentTrackingRepository.save(tracking);

        shipment.setStatus(request.getStatus());
        if (request.getStatus() == ShipmentStatus.PICKED_UP) {
            shipment.setPickedUpAt(now);
        } else if (request.getStatus() == ShipmentStatus.FAILED_DELIVERY || request.getStatus() == ShipmentStatus.CANCELLED) {
            shipment.setFailedAt(now);
        }
        shipment.setUpdatedAt(now);
        Shipment updated = shipmentRepository.save(shipment);

        ShipmentStatusChanged changedEvent = ShipmentStatusChanged.builder()
            .eventId(request.getEventId() == null || request.getEventId().isBlank() ? UUID.randomUUID().toString() : request.getEventId())
            .correlationId(request.getCorrelationId())
            .shipmentId(updated.getId())
            .subOrderId(updated.getSubOrderId())
            .shipmentType(updated.getShipmentType() != null ? updated.getShipmentType().name() : ShipmentType.OUTBOUND.name())
            .returnRequestId(updated.getReturnRequestId())
            .externalShipmentId(updated.getExternalShipmentId())
            .status(updated.getStatus().name())
            .trackingId(tracking.getTrackingId())
            .location(tracking.getLocation())
            .eta(tracking.getEta())
            .driverName(tracking.getDriverName())
            .driverPhone(tracking.getDriverPhone())
            .vehicle(tracking.getVehicle())
            .rawCarrierData(tracking.getRawCarrierData())
            .occurredAt(now)
            .build();
        shipmentEventProducer.sendShipmentStatusChanged(changedEvent);

        return shipmentQueryService.toResponse(updated);
    }

    private ShipmentResponse persistBookingFailed(
        SubOrderShippingRequested request,
        String carrierCode,
        ShipmentType shipmentType,
        String pickupAddress,
        String deliveryAddress
    ) {
        Instant now = Instant.now();
        Shipment failed = Shipment.builder()
            .subOrderId(request.getSubOrderId())
            .shipmentType(shipmentType == null ? ShipmentType.OUTBOUND : shipmentType)
            .returnRequestId(request.getReturnRequestId())
            .orderId(request.getOrderId())
            .vendorKeycloakId(request.getVendorKeycloakId())
            .carrierCode(carrierCode)
            .externalShipmentId("PENDING")
            .pickupAddress(pickupAddress)
            .deliveryAddress(deliveryAddress)
            .serviceLevel(request.getServiceLevel())
            .status(ShipmentStatus.BOOKING_FAILED)
            .createdAt(now)
            .updatedAt(now)
            .mocked(false)
            .build();
        Shipment savedFailed = shipmentRepository.save(failed);

        ShipmentStatusChanged failedEvent = ShipmentStatusChanged.builder()
            .eventId(UUID.randomUUID().toString())
            .correlationId(request.getCorrelationId())
            .shipmentId(savedFailed.getId())
            .subOrderId(savedFailed.getSubOrderId())
            .shipmentType(savedFailed.getShipmentType() != null ? savedFailed.getShipmentType().name() : ShipmentType.OUTBOUND.name())
            .returnRequestId(savedFailed.getReturnRequestId())
            .externalShipmentId(savedFailed.getExternalShipmentId())
            .status(savedFailed.getStatus().name())
            .occurredAt(now)
            .mocked(savedFailed.getMocked())
            .build();
        shipmentEventProducer.sendShipmentStatusChanged(failedEvent);

        return shipmentQueryService.toResponse(savedFailed);
    }

    private Shipment findExisting(SubOrderShippingRequested request, ShipmentType shipmentType) {
        if (shipmentType == ShipmentType.RETURN && request.getReturnRequestId() != null) {
            Shipment byReturn = shipmentRepository.findByReturnRequestId(request.getReturnRequestId()).orElse(null);
            if (byReturn != null) {
                return byReturn;
            }
        }
        return shipmentQueryService.findBySubOrderIdAndType(request.getSubOrderId(), shipmentType).orElse(null);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
