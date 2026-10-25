package com.tlcn.shipment_service.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderResponse {
    private Long id;
    private String orderCode;
    private List<SubOrderResponse> subOrders;
}
