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
 * Hibernate {@code ddl-auto=update} does not drop unused columns.
 * {@code carrier_order_id} duplicated {@code external_shipment_id};
 * {@code carrier_status} duplicated {@code status}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
@Slf4j
public class ShipmentCarrierColumnMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Boolean tableExists = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                  SELECT 1
                  FROM information_schema.tables
                  WHERE table_schema = current_schema()
                    AND table_name = 'shipments'
                )
                """,
                Boolean.class
            );
            if (!Boolean.TRUE.equals(tableExists)) {
                return;
            }
            jdbcTemplate.execute("ALTER TABLE shipments DROP COLUMN IF EXISTS carrier_order_id");
            jdbcTemplate.execute("ALTER TABLE shipments DROP COLUMN IF EXISTS carrier_status");
            log.info("Dropped unused shipments.carrier_order_id and shipments.carrier_status");
        } catch (Exception ex) {
            log.warn("Skip dropping unused carrier columns: {}", ex.getMessage());
        }
    }
}
