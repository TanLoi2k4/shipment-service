package com.tlcn.shipment_service.service;

import com.tlcn.shipment_service.dto.ActiveCarrierResponse;
import com.tlcn.shipment_service.dto.CarrierConfigRequest;
import com.tlcn.shipment_service.dto.CarrierConfigResponse;
import com.tlcn.shipment_service.exception.ConflictException;
import com.tlcn.shipment_service.exception.ResourceNotFoundException;
import com.tlcn.shipment_service.model.CarrierConfig;
import com.tlcn.shipment_service.repository.CarrierConfigRepository;
import com.tlcn.shipment_service.util.CarrierCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrierConfigService {

    private final CarrierConfigRepository carrierConfigRepository;

    public List<CarrierConfigResponse> getAllCarrierConfigs() {
        return carrierConfigRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<ActiveCarrierResponse> getActiveCarriers() {
        return carrierConfigRepository.findByActiveTrueOrderByIdAsc().stream()
            .map(config -> ActiveCarrierResponse.builder()
                .carrierCode(config.getCarrierCode())
                .carrierName(CarrierCatalog.displayName(config.getCarrierCode()))
                .build())
            .collect(Collectors.toList());
    }

    public CarrierConfigResponse getCarrierConfigById(Long id) {
        CarrierConfig config = carrierConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Carrier config not found: " + id));
        return toResponse(config);
    }

    public CarrierConfigResponse createCarrierConfig(CarrierConfigRequest request) {
        String carrierCode = request.getCarrierCode().trim();
        carrierConfigRepository.findByCarrierCodeIgnoreCase(carrierCode).ifPresent(existing -> {
            throw new ConflictException("Carrier config already exists: " + carrierCode, "DUPLICATE_RESOURCE");
        });
        CarrierConfig config = CarrierConfig.builder()
            .carrierCode(carrierCode)
            .baseUrl(request.getBaseUrl())
            .apiKey(request.getApiKey())
            .active(request.isActive())
            .build();
        return toResponse(carrierConfigRepository.save(config));
    }

    public CarrierConfigResponse updateCarrierConfig(Long id, CarrierConfigRequest request) {
        CarrierConfig config = carrierConfigRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Carrier config not found: " + id));
        String carrierCode = request.getCarrierCode().trim();
        carrierConfigRepository.findByCarrierCodeIgnoreCase(carrierCode)
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ConflictException("Carrier config already exists: " + carrierCode, "DUPLICATE_RESOURCE");
            });
        config.setCarrierCode(carrierCode);
        config.setBaseUrl(request.getBaseUrl());
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            config.setApiKey(request.getApiKey());
        }
        config.setActive(request.isActive());
        return toResponse(carrierConfigRepository.save(config));
    }

    public void deleteCarrierConfig(Long id) {
        if (!carrierConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Carrier config not found: " + id);
        }
        carrierConfigRepository.deleteById(id);
    }

    private CarrierConfigResponse toResponse(CarrierConfig config) {
        return CarrierConfigResponse.builder()
            .id(config.getId())
            .carrierCode(config.getCarrierCode())
            .baseUrl(config.getBaseUrl())
            .apiKey(config.getApiKey())
            .active(config.isActive())
            .build();
    }
}
