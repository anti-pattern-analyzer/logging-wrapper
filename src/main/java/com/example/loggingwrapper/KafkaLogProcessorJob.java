package com.example.loggingwrapper;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Quartz job to process logs and send them to Kafka in batches every 5 seconds.
 */
@Component
public class KafkaLogProcessorJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(KafkaLogProcessorJob.class);

    private static final String LOGS_LIST_KEY = "logs:list";
    private static final int BATCH_SIZE = 1000;

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaLogProcessorJob(StringRedisTemplate redisTemplate, KafkaTemplate<String, String> kafkaTemplate) {
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void execute(JobExecutionContext context) {
        logger.info("Executing scheduled log processor...");

        List<String> logs = redisTemplate.opsForList().range(LOGS_LIST_KEY, 0, BATCH_SIZE - 1);

        if (logs == null || logs.isEmpty()) {
            logger.info("No logs to process.");
            return;
        }

        // Send logs to Kafka asynchronously
        logs.forEach(logMessage -> kafkaTemplate.send("logs-topic", logMessage).whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Successfully sent log to Kafka: {}", logMessage);
            } else {
                logger.error("Failed to send log to Kafka: {}", logMessage, ex);
            }
        }));

        // Remove processed logs from Redis
        redisTemplate.opsForList().trim(LOGS_LIST_KEY, logs.size(), -1);

        logger.info("Processed {} logs and removed them from Redis.", logs.size());
    }
}
