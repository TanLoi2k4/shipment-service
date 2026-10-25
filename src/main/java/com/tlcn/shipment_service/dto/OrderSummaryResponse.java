package com.tlcn.shipment_service.dto;

import java.time.Instant;

import lombok.Data;

@Data
public class OrderSummaryResponse {
    private String orderCode;
    private Instant createdAt;
}
