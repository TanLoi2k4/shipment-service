package com.tlcn.shipment_service.dto;

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
public class ShipmentTrackingResponse {
    private Long id;
    private String trackingId;
    private String location;
    private Instant eta;
    private String driverName;
    private String driverPhone;
    private String vehicle;
    private String rawCarrierData;
    private Instant createdAt;
    private Instant updatedAt;
}
