# Order Enqueuing Service

## Description

This service receives, validates, stores and enqueues orders for subsequent
processing.  Simply put, it stores valid orders in an as yet undetermined
datasource and generates messages for a message queue whose where each event
will be consumed by instances of the **Order Processing Service**.

## Dev Environment Setup

To build, package, run and deploy this service the following software is required:
- Docker
- Java 8+
- Gradle
- IntelliJ

#### From IntelliJ

```    
MainClass: com.phizzard.es.App.Kt
Program arguments: src/main/resources/config.yml
Working directory: $MODULE_WORKING_DIR$
Environment variables:
            - HTTP_PORT=10080
```