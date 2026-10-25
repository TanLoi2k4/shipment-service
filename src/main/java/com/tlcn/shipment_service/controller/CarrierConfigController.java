package com.tlcn.shipment_service.controller;

import com.tlcn.shipment_service.dto.CarrierConfigRequest;
import com.tlcn.shipment_service.dto.CarrierConfigResponse;
import com.tlcn.shipment_service.dto.ResponseDTO;
import com.tlcn.shipment_service.service.CarrierConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/carrier-configs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class CarrierConfigController {

    private final CarrierConfigService carrierConfigService;

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CarrierConfigResponse>>> getAllCarrierConfigs() {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Carrier configs retrieved successfully", carrierConfigService.getAllCarrierConfigs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CarrierConfigResponse>> getCarrierConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Carrier config retrieved successfully", carrierConfigService.getCarrierConfigById(id)));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<CarrierConfigResponse>> createCarrierConfig(@Valid @RequestBody CarrierConfigRequest request) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Carrier config created successfully", carrierConfigService.createCarrierConfig(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CarrierConfigResponse>> updateCarrierConfig(@PathVariable Long id, @Valid @RequestBody CarrierConfigRequest request) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Carrier config updated successfully", carrierConfigService.updateCarrierConfig(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteCarrierConfig(@PathVariable Long id) {
        carrierConfigService.deleteCarrierConfig(id);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Carrier config deleted successfully", null));
    }
}
