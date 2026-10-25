package com.tlcn.shipment_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tlcn.shipment_service.dto.kafka.ShipmentCreated;
import com.tlcn.shipment_service.dto.kafka.ShipmentStatusChanged;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.shipment-created:shipment-created}")
    private String shipmentCreatedTopic;

    @Value("${spring.kafka.topics.shipment-status-changed:shipment-status-changed}")
    private String shipmentStatusChangedTopic;

    public void sendShipmentCreated(ShipmentCreated event) {
        if (event == null || event.getSubOrderId() == null) {
            return;
        }

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(Instant.now());
        }

        ProducerRecord<String, Object> record = new ProducerRecord<>(shipmentCreatedTopic, String.valueOf(event.getSubOrderId()), event);
        record.headers().add("messageId", event.getEventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("timestamp", String.valueOf(event.getCreatedAt() == null ? Instant.now().toEpochMilli() : event.getCreatedAt().toEpochMilli()).getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("Sent shipment-created: eventId={} shipmentId={} subOrderId={} -> topic={}", event.getEventId(), event.getShipmentId(), event.getSubOrderId(), shipmentCreatedTopic);
    }

    public void sendShipmentStatusChanged(ShipmentStatusChanged event) {
        if (event == null || event.getSubOrderId() == null) {
            return;
        }

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }

        ProducerRecord<String, Object> record = new ProducerRecord<>(shipmentStatusChangedTopic, String.valueOf(event.getSubOrderId()), event);
        record.headers().add("messageId", event.getEventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("timestamp", String.valueOf(event.getOccurredAt() == null ? Instant.now().toEpochMilli() : event.getOccurredAt().toEpochMilli()).getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
        log.info("Sent shipment-status-changed: eventId={} shipmentId={} subOrderId={} status={} -> topic={}", event.getEventId(), event.getShipmentId(), event.getSubOrderId(), event.getStatus(), shipmentStatusChangedTopic);
    }
}
