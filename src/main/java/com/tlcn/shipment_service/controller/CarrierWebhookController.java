package com.tlcn.shipment_service.controller;

import com.tlcn.shipment_service.dto.CarrierWebhookRequest;
import com.tlcn.shipment_service.dto.ResponseDTO;
import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.service.ShipmentLifecycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/carriers/webhook", "/api/carriers/webhook"})
@RequiredArgsConstructor
public class CarrierWebhookController {

    private final ShipmentLifecycleService shipmentLifecycleService;

    @PostMapping("/tracking")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> handleTrackingUpdate(@Valid @RequestBody CarrierWebhookRequest request) {
        ShipmentResponse response = shipmentLifecycleService.handleCarrierWebhook(request);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Tracking update processed successfully", response));
    }
}
