# LogBull Spring Boot Starter

Spring Boot auto-configuration for LogBull log collection system.

## Documentation

For complete documentation, usage examples, and API reference, please see the [main LogBull Java README](../README.md).

## Quick Start

### Maven

```xml
<dependency>
    <groupId>com.logbull</groupId>
    <artifactId>logbull-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.logbull:logbull-spring-boot-starter:1.0.0'
```

## Configuration

Add to your `application.yml`:

```yaml
logbull:
  enabled: true
  project-id: 12345678-1234-1234-1234-123456789012
  host: http://localhost:4005
  api-key: your-api-key
  log-level: INFO
```

Or `application.properties`:

```properties
logbull.enabled=true
logbull.project-id=12345678-1234-1234-1234-123456789012
logbull.host=http://localhost:4005
logbull.api-key=your-api-key
logbull.log-level=INFO
```

## Usage

Once configured, all logs from your Spring Boot application will automatically be sent to LogBull:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public void processPayment(String orderId, double amount) {
        logger.info("Processing payment for order: {}, amount: {}", orderId, amount);
    }
}
```

## Building

```bash
./gradlew :logbull-spring-boot-starter:build
```

## Testing

```bash
./gradlew :logbull-spring-boot-starter:test
```
