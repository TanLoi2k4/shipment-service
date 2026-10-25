package com.tlcn.shipment_service.exception;

/**
 * Thrown when a carrier booking fails due to a transient error so Kafka can retry.
 */
public class CarrierTransientException extends RuntimeException {
    public CarrierTransientException(String message) {
        super(message);
    }

    public CarrierTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}
