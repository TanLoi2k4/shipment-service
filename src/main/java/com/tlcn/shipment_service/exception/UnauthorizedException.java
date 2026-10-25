package com.tlcn.shipment_service.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, String errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
