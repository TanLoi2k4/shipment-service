package com.tlcn.shipment_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carrier_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carrier_code", nullable = false, unique = true, length = 80)
    private String carrierCode;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;

    @Column(name = "api_key", length = 512)
    private String apiKey;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
