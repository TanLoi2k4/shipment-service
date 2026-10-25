package com.tlcn.shipment_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate {@code ddl-auto=update} does not drop unused tables.
 * Tracking photos were stored as {@code @ElementCollection}; that mapping is gone.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
@Slf4j
public class ShipmentTrackingPhotosTableMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("DROP TABLE IF EXISTS shipment_tracking_photos");
            log.info("Dropped leftover table shipment_tracking_photos");
        } catch (Exception ex) {
            log.warn("Skip dropping shipment_tracking_photos: {}", ex.getMessage());
        }
    }
}
