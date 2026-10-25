package com.tlcn.shipment_service.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CarrierConfigRequest {

    @NotBlank(message = "carrierCode is required")
    private String carrierCode;

    @NotBlank(message = "baseUrl is required")
    private String baseUrl;

    private String apiKey;

    private boolean active = true;
}
