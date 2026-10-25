package com.tlcn.shipment_service.controller;

import com.tlcn.shipment_service.dto.ActiveCarrierResponse;
import com.tlcn.shipment_service.dto.ResponseDTO;
import com.tlcn.shipment_service.dto.ShipmentResponse;
import com.tlcn.shipment_service.dto.ShipmentTrackingResponse;
import com.tlcn.shipment_service.enums.ShipmentType;
import com.tlcn.shipment_service.service.CarrierConfigService;
import com.tlcn.shipment_service.service.ShipmentCustomerService;
import com.tlcn.shipment_service.service.ShipmentQueryService;
import com.tlcn.shipment_service.service.ShipmentVendorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShipmentRoleController {

    private final ShipmentQueryService shipmentQueryService;
    private final ShipmentCustomerService shipmentCustomerService;
    private final ShipmentVendorService shipmentVendorService;
    private final CarrierConfigService carrierConfigService;

    @GetMapping("/shipments/carriers")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ActiveCarrierResponse>>> getActiveCarriers() {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Active carriers retrieved successfully", carrierConfigService.getActiveCarriers()));
    }

    @GetMapping("/shipments/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ResponseDTO<List<ShipmentResponse>>> getVendorShipments(Authentication authentication) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Vendor shipments retrieved successfully", shipmentVendorService.getVendorShipments(authentication.getName())));
    }

    @GetMapping("/shipments/vendor/by-suborder/{subOrderId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> getVendorShipmentBySubOrder(
        Authentication authentication,
        @PathVariable Long subOrderId,
        @RequestParam(required = false) ShipmentType type
    ) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Vendor shipment retrieved successfully", shipmentVendorService.getVendorShipmentBySubOrder(subOrderId, authentication.getName(), type)));
    }

    @GetMapping("/shipments/vendor/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> getVendorShipmentById(
        Authentication authentication,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Vendor shipment retrieved successfully", shipmentVendorService.getVendorShipmentById(id, authentication.getName())));
    }

    @GetMapping("/shipments/vendor/{id}/tracking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ResponseDTO<List<ShipmentTrackingResponse>>> getVendorShipmentTracking(
        Authentication authentication,
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Vendor shipment tracking retrieved successfully", shipmentVendorService.getVendorShipmentTracking(id, authentication.getName())));
    }

    @GetMapping("/shipments/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<ShipmentResponse>>> getCustomerShipments(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return ResponseEntity.ok(new ResponseDTO<>(true, "Customer shipments retrieved successfully", shipmentCustomerService.getCustomerShipments(authorization)));
    }

    @GetMapping("/shipments/customer/by-suborder/{subOrderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> getCustomerShipmentBySubOrder(
        @PathVariable Long subOrderId,
        @RequestParam(required = false) ShipmentType type,
        HttpServletRequest request
    ) {
        String authorization = request.getHeader("Authorization");
        return ResponseEntity.ok(new ResponseDTO<>(true, "Customer shipment retrieved successfully", shipmentCustomerService.getCustomerShipmentBySubOrder(subOrderId, authorization, type)));
    }

    @GetMapping("/shipments/customer/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> getCustomerShipmentById(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        String authorization = request.getHeader("Authorization");
        return ResponseEntity.ok(new ResponseDTO<>(true, "Customer shipment retrieved successfully", shipmentCustomerService.getCustomerShipmentById(id, authorization)));
    }

    @GetMapping("/shipments/customer/{id}/tracking")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<ShipmentTrackingResponse>>> getCustomerShipmentTracking(
        @PathVariable Long id,
        HttpServletRequest request
    ) {
        String authorization = request.getHeader("Authorization");
        return ResponseEntity.ok(new ResponseDTO<>(true, "Customer shipment tracking retrieved successfully", shipmentCustomerService.getCustomerShipmentTracking(id, authorization)));
    }

    @GetMapping("/shipments/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ShipmentResponse>>> getAdminShipments() {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Admin shipments retrieved successfully", shipmentQueryService.getAllShipments()));
    }

    @GetMapping("/shipments/admin/vendor/{vendorKeycloakId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ShipmentResponse>>> getAdminShipmentsByVendor(@PathVariable String vendorKeycloakId) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Admin shipments by vendor retrieved successfully", shipmentQueryService.getShipmentsByVendor(vendorKeycloakId)));
    }

    @GetMapping("/shipments/admin/by-order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ShipmentResponse>>> getAdminShipmentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Admin shipments by order retrieved successfully", shipmentQueryService.getShipmentsByOrderId(orderId)));
    }

    @GetMapping("/shipments/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<ShipmentResponse>> getAdminShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Admin shipment retrieved successfully", shipmentQueryService.getShipmentById(id)));
    }

    @GetMapping("/shipments/admin/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ShipmentTrackingResponse>>> getAdminShipmentTracking(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>(true, "Admin shipment tracking retrieved successfully", shipmentQueryService.getShipmentTracking(id)));
    }
}
