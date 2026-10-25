package com.tlcn.shipment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierConfigResponse {
    private Long id;
    private String carrierCode;
    private String baseUrl;
    private String apiKey;
    private boolean active;
}
