package com.example.loggingwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for logging inter-service communication.
 * Logs are first stored in Redis (List) and processed in batches for Kafka.
 */
@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private static final String LOGS_LIST_KEY = "logs:list";

    // Logs older than this are deleted
    private static final long LOG_EXPIRATION_HOURS = 24;

    private final StringRedisTemplate redisTemplate;

    public LogService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Logs an inter-service request and stores it in Redis (List).
     */
    public void log(String sourceService, String destinationService, String method, String request, String response, boolean success) {
        String traceId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();

        String logMessage = String.format(
                "%s | source=%s, destination=%s, method=%s, trace_id=%s, request=%s, response=%s, success=%b",
                timestamp, sourceService, destinationService, method, traceId, request, response, success
        );

        saveToRedis(logMessage);
    }

    /**
     * Saves log entry to Redis List with TTL.
     */
    private void saveToRedis(String logMessage) {
        redisTemplate.opsForList().leftPush(LOGS_LIST_KEY, logMessage);
        redisTemplate.expire(LOGS_LIST_KEY, LOG_EXPIRATION_HOURS, TimeUnit.HOURS);
        logger.info("Log saved to Redis: {}", logMessage);
    }
}
