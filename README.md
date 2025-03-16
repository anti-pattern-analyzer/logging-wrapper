# Logging Wrapper Overview

This document provides an overview of the **Logging Wrapper**, a lightweight library (or service) that captures essential runtime data from within each microservice. It standardizes how logs are generated and sent to downstream systems (e.g., Kafka), so that the data can be further processed and visualized in end-to-end tracing and observability workflows.

---

## Table of Contents

1. [Purpose](#purpose)  
2. [High-Level Architecture](#high-level-architecture)  
3. [Key Components](#key-components)  
4. [Data Flow](#data-flow)  
5. [Typical Use Cases](#typical-use-cases)  
6. [Pitfalls and Recommendations](#pitfalls-and-recommendations)  
7. [Extensibility](#extensibility)  
8. [Contributing](#contributing)  

---

## Purpose

The Logging Wrapper’s primary goals:

- **Capture** critical runtime information (e.g., `trace_id`, request metadata, performance metrics).  
- **Enforce** a consistent logging format across all microservices (so logs can be easily parsed downstream).  
- **Publish** log entries to a message bus like Kafka asynchronously to reduce overhead.  
- **Bridge** the gap between internal service operations and external log processing pipelines.

---

## High-Level Architecture

1. **Library Integration**  
   - The Logging Wrapper typically resides as a Go (or your chosen language) library embedded in each microservice.  
   - Hooks into request handlers (HTTP/gRPC, etc.) or key code paths to intercept requests/responses and record metrics.

2. **Log Formatting**  
   - Common fields like `trace_id`, `span_id`, request type, timestamps, are captured.  
   - Additional metadata (e.g., environment, host, method name) may be appended.

3. **Publishing**  
   - Once a log record is formed, it is pushed to a Kafka topic or another transport system.  
   - Publishing is often done asynchronously, ensuring minimal latency impact on the core request.

4. **Metrics & Observability**  
   - The wrapper can optionally track performance data (e.g., request durations) and feed that into aggregated metrics or tracing pipelines.

---

## Key Components

### 1. Interceptor / Middleware
- **Implementation**: Often provided as a library function or middleware that wraps each incoming request.  
- **Responsibility**:  
  - Generates or re-uses an existing `trace_id` (if available).  
  - Creates a `span_id` for the current operation.  
  - Records request/response data like status code, latency, any errors.

### 2. Log Builder
- **Purpose**:  
  - Collects relevant metadata from context (user IDs, environment, request payload details, etc.).  
  - Ensures logs meet a unified schema (JSON, Proto, etc.).

### 3. Async Publisher
- **Description**:  
  - Sends the finalized log record to Kafka or another messaging system.  
  - Usually buffers or batches messages to minimize overhead.

---

## Data Flow

1. **Incoming Request**  
   - The microservice receives an HTTP/gRPC request.  
   - The Logging Wrapper intercepts the request, assigning or inheriting a `trace_id`.

2. **Local Processing**  
   - The microservice performs its normal business logic.  
   - During or after execution, the wrapper collects timestamps, request metadata, status, etc.

3. **Log Record Construction**  
   - A final log entry is built (including fields such as `trace_id`, `span_id`, request method, latency, errors, etc.).  
   - The record is serialized (e.g. JSON).

4. **Asynchronous Publish**  
   - The record is queued and published to Kafka (or your chosen log sink).

5. **Further Pipeline**  
   - The log is consumed by external tools (e.g., the Log Processor) for storing, correlation, or dependency graph updates.

---

## Typical Use Cases

- **Distributed Tracing**  
  - Enforces consistent `trace_id` usage across microservices, enabling end-to-end correlation in a tracing pipeline.

- **Request/Response Auditing**  
  - Logs essential data (e.g., user IDs, query parameters) for audits or debugging.

- **Performance Monitoring**  
  - Captures latency and status codes to identify slow endpoints or error patterns.

---

## Pitfalls and Recommendations

1. **Excessive Log Volume**  
   - Logging every single detail can become expensive and flood Kafka. Consider sampling strategies or debug toggles.

2. **Latency Impact**  
   - Ensure asynchronous publishing. Avoid blocking the main request flow if the message broker is unavailable.

3. **Trace Propagation**  
   - Make certain `trace_id` is passed to downstream calls (HTTP headers, gRPC metadata) so logs remain correlated.

4. **Failover Handling**  
   - If Kafka is down or slow, define fallback behavior. Buffer in memory? Drop logs?

---

## Extensibility

- **Custom Fields**  
  - Add domain-specific metadata (e.g., user roles, transaction IDs) to logs.  
- **Alternate Transports**  
  - Switch out Kafka for RabbitMQ, NATS, or direct HTTP-based log ingestion with minimal changes.  
- **Extended Metrics**  
  - Integrate with Prometheus or statsd to expose metrics about local processing times, success/failure ratios, etc.

---

## Contributing

1. **Fork the repository** and create a new feature branch.  
2. **Implement** your changes or bug fixes, adding relevant tests if possible.  
3. **Open a Pull Request** describing your modifications.

---
