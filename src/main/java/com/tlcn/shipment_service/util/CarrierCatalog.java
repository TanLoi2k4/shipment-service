package com.tlcn.shipment_service.util;

import java.util.Map;

public final class CarrierCatalog {

    private static final Map<String, String> DISPLAY_NAMES = Map.of(
        "GIAOHANG_NHANH", "Giao Hàng Nhanh",
        "GHTK", "Giao Hàng Tiết Kiệm",
        "VIETTEL_POST", "Viettel Post"
    );

    private CarrierCatalog() {
    }

    public static String displayName(String carrierCode) {
        if (carrierCode == null || carrierCode.isBlank()) {
            return null;
        }
        return DISPLAY_NAMES.getOrDefault(carrierCode, carrierCode);
    }
}
