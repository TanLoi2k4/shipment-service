package com.tlcn.shipment_service.kafka;

import com.tlcn.shipment_service.dto.kafka.SubOrderShippingRequested;
import com.tlcn.shipment_service.service.ShipmentLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentRequestConsumer {

    private static final String IDEMPOTENCY_KEY_PREFIX = "kafka:shipment:request:idempotency:";

    private final ShipmentLifecycleService shipmentLifecycleService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${kafka.idempotency.ttl-hours:24}")
    private long idempotencyTtlHours;

    @KafkaListener(
        topics = "${spring.kafka.topics.suborder-shipping-requested:suborder-shipping-requested}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeSubOrderShippingRequested(
        SubOrderShippingRequested event,
        @Header(name = "messageId", required = false) String messageId,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment ack
    ) {
        try {
            if (event == null || event.getSubOrderId() == null) {
                log.warn("Ignore invalid suborder-shipping-requested event: messageId={} topic={} partition={} offset={}", messageId, topic, partition, offset);
                ack.acknowledge();
                return;
            }

            String eventId = normalizeEventId(event.getEventId(), messageId);
            if (isAlreadyProcessed(eventId)) {
                log.info("Skip duplicate suborder-shipping-requested: eventId={} subOrderId={}", eventId, event.getSubOrderId());
                ack.acknowledge();
                return;
            }

            shipmentLifecycleService.handleShipmentRequest(event);
            markAsProcessed(eventId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process suborder-shipping-requested: subOrderId={} messageId={} error={}",
                event == null ? null : event.getSubOrderId(),
                messageId,
                e.getMessage(),
                e
            );
            throw e;
        }
    }

    private String normalizeEventId(String eventId, String fallback) {
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }
        return fallback == null || fallback.isBlank() ? null : fallback;
    }

    private boolean isAlreadyProcessed(String key) {
        return key != null && Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(key)));
    }

    private void markAsProcessed(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(buildKey(key), "processed", Math.max(1L, idempotencyTtlHours), TimeUnit.HOURS);
    }

    private String buildKey(String key) {
        return IDEMPOTENCY_KEY_PREFIX + key;
    }
}
