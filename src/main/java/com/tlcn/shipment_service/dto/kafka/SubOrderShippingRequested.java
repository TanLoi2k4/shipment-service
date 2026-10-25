package com.tlcn.shipment_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubOrderShippingRequested {
    private String eventId;
    private String correlationId;
    private Long subOrderId;
    private Long orderId;
    private String vendorKeycloakId;
    private String pickupAddress;
    private String deliveryAddress;
    private String serviceLevel;
    private String carrierPreference;
    private String shipmentType;
    private Long returnRequestId;
    private Instant createdAt;

    public boolean isReturnShipment() {
        return returnRequestId != null || "RETURN".equalsIgnoreCase(shipmentType);
    }
}
