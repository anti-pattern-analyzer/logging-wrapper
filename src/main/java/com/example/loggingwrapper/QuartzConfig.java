package com.example.loggingwrapper;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail kafkaLogProcessorJobDetail() {
        return JobBuilder.newJob(KafkaLogProcessorJob.class)
                .withIdentity("kafkaLogProcessorJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger kafkaLogProcessorTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(kafkaLogProcessorJobDetail())
                .withIdentity("kafkaLogProcessorTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();
    }
}
