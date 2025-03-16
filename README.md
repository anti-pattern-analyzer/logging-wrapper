# Logging Wrapper

The Logger Service is a microservice designed to capture and manage logs efficiently in a distributed system. It collects log messages from various services and publishes them to Kafka for further processing, storage, and analysis.

## Overview
The logging mechanism ensures real-time log streaming and facilitates centralized logging, making it easier to debug, monitor, and analyze system behavior.

## Architecture
The service follows a producer-consumer model where:

![image](https://github.com/user-attachments/assets/66aac47b-3c04-47af-b4da-22c0d32f4952)

1. Logger Service captures logs from applications.
2. Logs are published to a Kafka topic, enabling scalable and fault-tolerant log processing.

This setup allows seamless log aggregation, observability, and tracing across microservices.
