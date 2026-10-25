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
public class ShipmentStatusChanged {
    private String eventId;
    private String correlationId;
    private Long shipmentId;
    private Long subOrderId;
    private String shipmentType;
    private Long returnRequestId;
    private String externalShipmentId;
    private String status;
    private String trackingId;
    private String location;
    private Instant eta;
    private String driverName;
    private String driverPhone;
    private String vehicle;
    private String rawCarrierData;
    private Instant occurredAt;
    private Boolean mocked;
}
