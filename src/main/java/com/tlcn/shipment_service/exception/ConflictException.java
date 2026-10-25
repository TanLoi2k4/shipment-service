package com.tlcn.shipment_service.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String message, String errorCode) {
        super(message, HttpStatus.CONFLICT, errorCode);
    }
}
