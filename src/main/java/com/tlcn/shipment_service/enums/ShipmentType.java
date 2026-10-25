package com.tlcn.shipment_service.enums;

public enum ShipmentType {
    OUTBOUND,
    RETURN;

    public static ShipmentType fromEvent(String value) {
        if (value == null || value.isBlank()) {
            return OUTBOUND;
        }
        try {
            return ShipmentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OUTBOUND;
        }
    }

    public boolean isReturn() {
        return this == RETURN;
    }
}
