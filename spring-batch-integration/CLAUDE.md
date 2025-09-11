# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Overview

Spring Batch Integration provides integration between Spring Batch and Spring Integration projects. This module enables scaling and externalizing batch processing through messaging infrastructure.

## Build & Development Commands

### Essential Commands
- **Build module:** `../mvnw clean package`
- **Run tests:** `../mvnw test`
- **Run specific test:** `../mvnw test -Dtest=ClassName#methodName`
- **Run integration tests:** `../mvnw verify` (requires Docker for Artemis JMS broker)
- **Skip tests:** `../mvnw package -DskipTests`
- **Format code:** `../mvnw spring-javaformat:apply`
- **Check formatting:** `../mvnw spring-javaformat:check`

### Parent Project Commands (from parent directory)
- **Build entire Spring Batch:** `./mvnw clean install`
- **Build with all integration tests:** `./mvnw verify`

## Architecture & Core Components

### Package Structure
```
org.springframework.batch.integration
├── async/           # Asynchronous item processing
├── chunk/           # Remote chunking support
├── config/          # Configuration and annotations
│   ├── annotation/  # @EnableBatchIntegration
│   └── xml/         # XML namespace support
├── launch/          # Job launching via messages
└── partition/       # Remote partitioning support
```

### Key Features

#### 1. Remote Chunking
Splits processing across multiple JVMs:
- **Manager:** Reads items and sends chunks to workers via messaging
- **Worker:** Processes chunks and returns results
- Classes: `RemoteChunkingManagerStepBuilder`, `RemoteChunkingWorkerBuilder`
- Use when processing is the bottleneck

#### 2. Remote Partitioning
Distributes complete step executions to workers:
- **Manager:** Creates partitions and delegates to workers
- **Worker:** Executes complete step for assigned partition
- Classes: `RemotePartitioningManagerStepBuilder`, `RemotePartitioningWorkerStepBuilder`
- Use when you can partition data (e.g., by date range, ID range)

#### 3. Async Processing
Processes items asynchronously within same JVM:
- `AsyncItemProcessor`: Delegates to Future-returning processor
- `AsyncItemWriter`: Unwraps Futures and delegates to actual writer
- Use for I/O bound processing

#### 4. Job Launching
Launch jobs via Spring Integration messages:
- `JobLaunchingGateway`: Request/reply pattern
- `JobLaunchingMessageHandler`: Fire-and-forget pattern
- `JobLaunchRequest`: Message payload for job parameters

### Configuration Patterns

**Enable Integration Features:**
```java
@Configuration
@EnableBatchProcessing
@EnableBatchIntegration
public class BatchIntegrationConfig {
    // Provides RemoteChunkingManagerStepBuilderFactory
    // and RemotePartitioningManagerStepBuilderFactory
}
```

**Remote Chunking Manager:**
```java
@Bean
public Step managerStep() {
    return remoteChunkingManagerStepBuilderFactory.get("managerStep")
        .<Input, Output>chunk(100)
        .reader(itemReader())
        .outputChannel(requests())
        .inputChannel(replies())
        .build();
}
```

**Remote Chunking Worker:**
```java
@Bean
public IntegrationFlow workerFlow() {
    return remoteChunkingWorkerBuilder
        .itemProcessor(processor())
        .itemWriter(writer())
        .inputChannel(requests())
        .outputChannel(replies())
        .build();
}
```

## Testing Guidelines

### Test Categories
- Unit tests: `*Tests.java` - Test individual components
- Integration tests: `*IntegrationTests.java` - Test with real messaging infrastructure
- Use `@SpringBatchTest` for batch-specific test setup
- Artemis JMS broker used for integration tests (auto-started)

### Common Test Patterns
```java
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class RemoteChunkingIntegrationTests {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    
    @Test
    void testRemoteChunking() {
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
```

## Dependencies

### Core Dependencies
- `spring-batch-core`: Core batch functionality
- `spring-integration-core`: Spring Integration framework
- `spring-messaging`: Message abstractions

### Optional Dependencies
- `spring-integration-jms`: JMS support
- `spring-integration-jdbc`: JDBC channel support
- `jakarta.jms-api`: JMS API

### Test Dependencies
- `spring-batch-test`: Test utilities
- `artemis-server`: Embedded JMS broker for tests
- `hsqldb`: In-memory database for tests

## Common Development Tasks

### Adding Remote Processing to Existing Step
1. Add `@EnableBatchIntegration` to configuration
2. Configure messaging infrastructure (channels, connection factory)
3. Replace regular StepBuilder with RemoteChunking/PartitioningManagerStepBuilder
4. Implement worker configuration in separate application/profile

### Debugging Remote Processing
- Enable DEBUG logging: `org.springframework.batch.integration`
- Monitor message channels using Spring Integration utilities
- Check JobRepository for step execution status
- Use correlation IDs to track chunks/partitions

### Performance Tuning
- Chunk size: Balance between network overhead and processing efficiency
- Concurrent workers: Set based on available resources
- Message acknowledgment: Configure based on reliability requirements
- Timeout settings: Adjust based on processing time expectations

## Important Considerations

1. **Transaction Management:** Remote chunks/partitions have separate transactions
2. **Error Handling:** Configure retry and skip policies appropriately
3. **Message Ordering:** Not guaranteed in distributed processing
4. **State Management:** Use JobRepository for persistent state
5. **Resource Cleanup:** Ensure channels and connections are properly closed
6. **Serialization:** Items must be serializable for remote processing