package com.tlcn.shipment_service.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderPageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
