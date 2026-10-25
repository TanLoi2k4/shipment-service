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
public class ShipmentCreated {
    private String eventId;
    private String correlationId;
    private Long shipmentId;
    private Long subOrderId;
    private String shipmentType;
    private Long returnRequestId;
    private String externalShipmentId;
    private String carrierCode;
    private String status;
    private Instant createdAt;
    private Boolean mocked;
}
