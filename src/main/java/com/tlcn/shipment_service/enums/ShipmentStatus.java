package com.tlcn.shipment_service.enums;

public enum ShipmentStatus {
    CREATED,
    BOOKING_FAILED,
    BOOKED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED,
    FAILED_DELIVERY,
    RETURNING,
    RETURNED,
    CANCELLED;

    public boolean isTerminal() {
        return this == DELIVERED || this == RETURNED || this == CANCELLED;
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        if (next == null) {
            return false;
        }

        if (this == next) {
            return true;
        }

        return switch (this) {
            case CREATED -> next == BOOKED || next == BOOKING_FAILED || next == CANCELLED;
            case BOOKING_FAILED -> next == BOOKED || next == CANCELLED;
            case BOOKED -> next == PICKED_UP || next == FAILED_DELIVERY || next == CANCELLED;
            case PICKED_UP -> next == IN_TRANSIT || next == DELIVERED || next == FAILED_DELIVERY || next == RETURNING || next == CANCELLED;
            case IN_TRANSIT -> next == DELIVERED || next == FAILED_DELIVERY || next == RETURNING || next == CANCELLED;
            case DELIVERED -> false;
            case FAILED_DELIVERY -> next == RETURNING || next == RETURNED || next == CANCELLED;
            case RETURNING -> next == RETURNED || next == CANCELLED;
            case RETURNED -> false;
            case CANCELLED -> false;
        };
    }
}
