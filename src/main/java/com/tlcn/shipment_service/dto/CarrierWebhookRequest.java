package com.tlcn.shipment_service.dto;

import com.tlcn.shipment_service.enums.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CarrierWebhookRequest {

    private String eventId;
    private String correlationId;

    @NotBlank(message = "externalShipmentId is required")
    private String externalShipmentId;

    private String trackingId;

    @NotNull(message = "status is required")
    private ShipmentStatus status;

    private String location;
    private Instant eta;
    private String driverName;
    private String driverPhone;
    private String vehicle;
    private String rawCarrierData;
}
