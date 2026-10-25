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
 * Backfill outbound type so a sub-order can also have a RETURN shipment.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class ShipmentTypeMigrator implements ApplicationRunner {

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
            jdbcTemplate.execute(
                """
                ALTER TABLE shipments
                ADD COLUMN IF NOT EXISTS shipment_type varchar(16) DEFAULT 'OUTBOUND'
                """
            );
            jdbcTemplate.execute(
                """
                ALTER TABLE shipments
                ADD COLUMN IF NOT EXISTS return_request_id bigint
                """
            );
            int updated = jdbcTemplate.update(
                "UPDATE shipments SET shipment_type = 'OUTBOUND' WHERE shipment_type IS NULL"
            );
            if (updated > 0) {
                log.info("Backfilled shipment_type=OUTBOUND for {} rows", updated);
            }
        } catch (Exception ex) {
            log.warn("Could not backfill shipment_type: {}", ex.getMessage());
        }
    }
}
