package com.tlcn.shipment_service.config;

import com.tlcn.shipment_service.enums.ShipmentStatus;
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

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Hibernate {@code ddl-auto=update} does not refresh PostgreSQL CHECK constraints
 * when enum values change. Keep {@code shipments_status_check} aligned with
 * {@link ShipmentStatus}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class ShipmentStatusConstraintMigrator implements ApplicationRunner {

    private static final String CONSTRAINT_NAME = "shipments_status_check";

    private final JdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

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

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                jdbcTemplate.execute("ALTER TABLE shipments DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME);
                jdbcTemplate.update("UPDATE shipments SET status = 'BOOKED' WHERE status = 'PENDING'");
                jdbcTemplate.update("UPDATE shipments SET status = 'IN_TRANSIT' WHERE status = 'DELIVERING'");
                jdbcTemplate.execute(
                    "ALTER TABLE shipments ADD CONSTRAINT " + CONSTRAINT_NAME
                        + " CHECK (status IN (" + allowedStatusesSql() + "))"
                );
            });
            log.info("Ensured {} allows current ShipmentStatus values", CONSTRAINT_NAME);
        } catch (Exception e) {
            log.warn("Skip shipment status constraint migration: {}", e.getMessage());
        }
    }

    private String allowedStatusesSql() {
        return Arrays.stream(ShipmentStatus.values())
            .map(value -> "'" + value.name() + "'")
            .collect(Collectors.joining(", "));
    }
}
