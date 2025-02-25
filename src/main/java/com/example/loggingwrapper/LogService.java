package com.example.loggingwrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private static final String LOGS_LIST_KEY = "logs:list";

    private static final long LOG_EXPIRATION_HOURS = 24;

    private final StringRedisTemplate redisTemplate;

    public LogService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void log(String sourceService, String destinationService, String method, String request,
                    String response, String traceId, String spanId, String parentSpanId) {

        String timestamp = LocalDateTime.now().toString();

        String logMessage = String.format(
                "%s | trace_id=%s, span_id=%s, parent_span_id=%s, source=%s, destination=%s, method=%s, request=%s, response=%s",
                timestamp, traceId, spanId, parentSpanId, sourceService, destinationService, method, request, response
        );

        saveToRedis(logMessage, traceId, spanId);
    }

    private void saveToRedis(String logMessage, String traceId, String spanId) {
        redisTemplate.opsForList().leftPush(LOGS_LIST_KEY, logMessage);
        redisTemplate.expire(LOGS_LIST_KEY, LOG_EXPIRATION_HOURS, TimeUnit.HOURS);

        redisTemplate.opsForValue().set("trace:" + spanId, traceId, LOG_EXPIRATION_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForList().leftPush("trace_structure:" + traceId, spanId);

        logger.info("Log saved to Redis: {}", logMessage);
    }
}
