package com.example.loggingwrapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for logging inter-service communication.
 * <p>
 * This service stores log entries in Redis, capturing details
 * about API calls between microservices, including:
 * - Source service
 * - Destination service
 * - API method
 * - Request and response payloads
 * - Success status
 * </p>
 */
@Service
public class LogService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Constructor to initialize RedisTemplate.
     *
     * @param redisTemplate Spring's RedisTemplate for interacting with Redis.
     */
    public LogService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Logs an inter-service request and stores it in Redis.
     *
     * @param sourceService      The name of the service making the request.
     * @param destinationService The name of the service receiving the request.
     * @param method             The API method being called.
     * @param request            The request payload.
     * @param response           The response payload.
     * @param success            Whether the request was successful.
     */
    public void log(String sourceService, String destinationService, String method, String request, String response, boolean success) {
        String traceId = UUID.randomUUID().toString();
        String logMessage = String.format(
                "%s | source=%s, destination=%s, method=%s, trace_id=%s, request=%s, response=%s, success=%b",
                LocalDateTime.now(), sourceService, destinationService, method, traceId, request, response, success
        );

        // Save to Redis List
        redisTemplate.opsForList().leftPush("logs", logMessage);
    }
}
