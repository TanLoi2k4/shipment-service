package com.tlcn.shipment_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;

    public ResponseDTO(boolean success, String message, T data) {
        this(success, message, data, null);
    }

    public static <T> ResponseDTO<T> ok(String message, T data) {
        return new ResponseDTO<>(true, message, data, null);
    }

    public static <T> ResponseDTO<T> fail(String message, String errorCode) {
        return new ResponseDTO<>(false, message, null, errorCode);
    }
}
