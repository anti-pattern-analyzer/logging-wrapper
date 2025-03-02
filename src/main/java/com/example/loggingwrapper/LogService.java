package com.example.loggingwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final String KAFKA_TOPIC = "logs-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public LogService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void log(String sourceService, String destinationService, String method, String type, String request,
                    String response, String traceId, String spanId, String parentSpanId) {

        Long timestamp = Instant.now().toEpochMilli();

        String logMessage = String.format(
                "%s | trace_id=%s, span_id=%s, parent_span_id=%s, source=%s, destination=%s, method=%s, type=%s, request=%s, response=%s",
                timestamp, traceId, spanId, parentSpanId, sourceService, destinationService, method, type, request, response
        );

        kafkaTemplate.send(KAFKA_TOPIC, spanId, logMessage)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Log sent to Kafka: {}", logMessage);
                    } else {
                        logger.error("Failed to send log to Kafka: {}", logMessage, ex);
                    }
                });
    }
}
