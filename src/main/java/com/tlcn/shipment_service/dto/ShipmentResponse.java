package com.tlcn.shipment_service.dto;

import com.tlcn.shipment_service.enums.ShipmentStatus;
import com.tlcn.shipment_service.enums.ShipmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentResponse {

    private Long id;
    private Long subOrderId;
    private Long orderId;
    private ShipmentType shipmentType;
    private Long returnRequestId;
    private String pickupAddress;
    private String deliveryAddress;
    private String externalShipmentId;
    private String carrierCode;
    private String carrierName;
    private String carrierTrackingId;
    private String serviceLevel;
    private String externalReference;
    private ShipmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean mocked;
}
