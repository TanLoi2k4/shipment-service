package com.tlcn.shipment_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Hibernate {@code @Lob} on PostgreSQL maps String to OID large objects.
 * Those cannot be read in auto-commit mode, which caused 500s on tracking APIs.
 * This converts leftover oid columns to TEXT after the mapping change.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class ShipmentTrackingLobMigrator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Boolean isOid = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                  SELECT 1
                  FROM information_schema.columns
                  WHERE table_schema = current_schema()
                    AND table_name = 'shipment_tracking'
                    AND column_name = 'raw_carrier_data'
                    AND udt_name = 'oid'
                )
                """,
                Boolean.class
            );
            if (!Boolean.TRUE.equals(isOid)) {
                return;
            }

            log.info("Converting shipment_tracking.raw_carrier_data from oid to TEXT");
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> jdbcTemplate.execute(
                """
                ALTER TABLE shipment_tracking
                ALTER COLUMN raw_carrier_data TYPE TEXT
                USING (
                  CASE
                    WHEN raw_carrier_data IS NULL THEN NULL
                    ELSE convert_from(loread(lo_open(raw_carrier_data::oid, 262144), 10000000), 'UTF8')
                  END
                )
                """
            ));
            log.info("Converted shipment_tracking.raw_carrier_data to TEXT");
        } catch (Exception e) {
            log.warn("Skip raw_carrier_data oid->text migration: {}", e.getMessage());
        }
    }
}
