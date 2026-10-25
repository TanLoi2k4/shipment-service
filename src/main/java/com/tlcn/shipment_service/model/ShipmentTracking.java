package com.tlcn.shipment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "shipment_tracking", uniqueConstraints = {
    @UniqueConstraint(name = "uk_shipment_tracking_tracking_id", columnNames = "tracking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "tracking_id", nullable = false, unique = true, length = 120)
    private String trackingId;

    @Column(length = 255)
    private String location;

    @Column(name = "eta")
    private Instant eta;

    @Column(name = "driver_name", length = 120)
    private String driverName;

    @Column(name = "driver_phone", length = 40)
    private String driverPhone;

    @Column(length = 120)
    private String vehicle;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "raw_carrier_data", columnDefinition = "TEXT")
    private String rawCarrierData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
