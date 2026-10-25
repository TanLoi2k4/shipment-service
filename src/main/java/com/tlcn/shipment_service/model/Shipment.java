package com.tlcn.shipment_service.model;

import com.tlcn.shipment_service.enums.ShipmentStatus;
import com.tlcn.shipment_service.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sub_order_id", nullable = false)
    private Long subOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_type", length = 16)
    @Builder.Default
    private ShipmentType shipmentType = ShipmentType.OUTBOUND;

    @Column(name = "return_request_id")
    private Long returnRequestId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "vendor_keycloak_id", length = 120)
    private String vendorKeycloakId;

    @Column(name = "carrier_code", nullable = false, length = 80)
    private String carrierCode;

    @Column(name = "external_shipment_id", nullable = false, length = 120)
    private String externalShipmentId;

    @Column(name = "pickup_address", length = 512)
    private String pickupAddress;

    @Column(name = "delivery_address", length = 512)
    private String deliveryAddress;

    @Column(name = "service_level", length = 80)
    private String serviceLevel;

    @Column(name = "mocked", nullable = false)
    @Builder.Default
    private Boolean mocked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "booked_at")
    private Instant bookedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
