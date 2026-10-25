package com.tlcn.shipment_service.service;

import com.tlcn.shipment_service.model.CarrierConfig;
import com.tlcn.shipment_service.model.Shipment;
import com.tlcn.shipment_service.dto.kafka.SubOrderShippingRequested;
import com.tlcn.shipment_service.repository.CarrierConfigRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarrierIntegrationService {

    private final CarrierConfigRepository carrierConfigRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${carrier.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${carrier.default-code:GIAOHANG_NHANH}")
    private String defaultCarrierCode;

    @Value("${carrier.base-url:https://api.carrier.example}")
    private String fallbackBaseUrl;

    /**
     * Resolves an admin-configured active carrier. When mock is enabled and no
     * active unit exists, falls back to default-code so local booking still works.
     */
    public Optional<CarrierConfig> resolveCarrierConfig(String preferredCarrierCode) {
        String carrierCode = normalizeCarrierCode(preferredCarrierCode);

        if (carrierCode != null && !"ANY".equals(carrierCode)) {
            Optional<CarrierConfig> config = carrierConfigRepository.findByCarrierCodeIgnoreCaseAndActiveTrue(carrierCode);
            if (config.isPresent()) {
                return config;
            }
            if (mockEnabled) {
                return Optional.of(fallbackConfig(carrierCode));
            }
            return Optional.empty();
        }

        Optional<CarrierConfig> firstActive = carrierConfigRepository.findFirstByActiveTrueOrderByIdAsc();
        if (firstActive.isPresent()) {
            return firstActive;
        }
        if (mockEnabled) {
            return Optional.of(fallbackConfig(defaultCarrierCode));
        }
        return Optional.empty();
    }

    private CarrierConfig fallbackConfig(String carrierCode) {
        return CarrierConfig.builder()
            .carrierCode(carrierCode)
            .baseUrl(fallbackBaseUrl)
            .active(true)
            .build();
    }

    public CarrierBookingResult bookShipment(Shipment shipment, SubOrderShippingRequested request, CarrierConfig config) {
        String carrierCode = config.getCarrierCode();

        if (mockEnabled) {
            String externalShipmentId = generateExternalShipmentId(carrierCode, request.getSubOrderId());
            String rawResponse = "{\"mock\":true,\"carrierCode\":\"" + carrierCode + "\",\"externalShipmentId\":\"" + externalShipmentId + "\"}";
            log.info("Mock carrier booking: carrierCode={} subOrderId={} externalShipmentId={}", carrierCode, request.getSubOrderId(), externalShipmentId);
            return new CarrierBookingResult(true, externalShipmentId, rawResponse, true, false);
        }

        String endpoint = trimTrailingSlash(config.getBaseUrl()) + "/shipments/book";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            headers.set("X-API-Key", config.getApiKey());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subOrderId", request.getSubOrderId());
        payload.put("orderId", request.getOrderId());
        payload.put("vendorKeycloakId", request.getVendorKeycloakId());
        payload.put("pickupAddress", request.getPickupAddress());
        payload.put("deliveryAddress", request.getDeliveryAddress());
        payload.put("serviceLevel", request.getServiceLevel());
        payload.put("carrierPreference", request.getCarrierPreference());
        payload.put("shipmentType", request.isReturnShipment() ? "RETURN" : "OUTBOUND");
        payload.put("returnRequestId", request.getReturnRequestId());
        payload.put("shipmentId", shipment.getId());

        String rawResponse;
        try {
            rawResponse = restTemplate.postForObject(URI.create(endpoint), new HttpEntity<>(payload, headers), String.class);
        } catch (Exception ex) {
            log.warn("Carrier booking call failed: carrierCode={} subOrderId={} error={}", carrierCode, request.getSubOrderId(), ex.getMessage());
            return new CarrierBookingResult(false, null, "{\"error\":\"" + ex.getMessage() + "\"}", false, true);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            return new CarrierBookingResult(false, null, "{\"error\":\"empty carrier response\"}", false, false);
        }

        // Try to parse externalShipmentId from carrier JSON response. DO NOT fabricate an id for real carrier responses.
        ObjectMapper mapper = new ObjectMapper();
        String externalShipmentId = null;
        try {
            JsonNode root = mapper.readTree(rawResponse);
            if (root.has("externalShipmentId")) {
                externalShipmentId = root.get("externalShipmentId").asText();
            } else if (root.has("external_shipment_id")) {
                externalShipmentId = root.get("external_shipment_id").asText();
            } else if (root.has("id")) {
                externalShipmentId = root.get("id").asText();
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse carrier response JSON: {}", e.getMessage());
        }

        if (externalShipmentId == null || externalShipmentId.isBlank()) {
            return new CarrierBookingResult(false, null, rawResponse, false, false);
        }

        return new CarrierBookingResult(true, externalShipmentId, rawResponse, false, false);
    }

    private String normalizeCarrierCode(String carrierCode) {
        if (carrierCode == null || carrierCode.isBlank()) {
            return null;
        }
        return carrierCode.trim().toUpperCase();
    }

    private String generateExternalShipmentId(String carrierCode, Long subOrderId) {
        return carrierCode + "-" + subOrderId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CarrierBookingResult {
        private boolean success;
        private String externalShipmentId;
        private String rawResponse;
        private boolean mock;
        private boolean transientError;
    }
}
