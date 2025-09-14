# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build Commands
- `./mvnw package` - Build project without integration tests
- `./mvnw verify` - Full build with integration tests (requires Docker)
- `./mvnw test` - Run unit tests only (excludes *IntegrationTests.java and *FunctionalTests.java)
- `./mvnw test -Dtest=ClassName#methodName` - Run specific test method
- `./mvnw test -Dtest=ClassName` - Run all tests in a specific class
- `./mvnw spring-javaformat:apply` - Format code according to Spring conventions (run before committing)

### Development Commands from Parent Directory
- `cd spring-batch-infrastructure && ../mvnw test` - Run tests for infrastructure module only
- `../mvnw verify -pl spring-batch-infrastructure` - Run integration tests for infrastructure module

## Architecture

### Module Context
The spring-batch-infrastructure module is part of a multi-module Spring Batch project. It provides foundational item processing components but does NOT contain:
- Job/Step execution logic (located in spring-batch-core)
- @EnableBatchProcessing and other configuration annotations (in spring-batch-core)
- JobRepository, JobLauncher, JobExplorer (in spring-batch-core)

### Core Item Processing Pattern
Spring Batch uses a Reader-Processor-Writer pattern for batch processing:

1. **ItemReader<T>** (`org.springframework.batch.item.ItemReader`)
   - Stateful, returns null when exhausted
   - Must be thread-safe if used in multi-threaded steps
   - Key implementations in subpackages: file, database, json, xml, kafka, jms

2. **ItemProcessor<I,O>** (`org.springframework.batch.item.ItemProcessor`)
   - Transforms items, returns null to filter
   - Can change types (I → O)

3. **ItemWriter<T>** (`org.springframework.batch.item.ItemWriter`)
   - Writes chunks of items (not individual items)
   - Method signature: `write(Chunk<? extends T> chunk)`

### Key Packages
- `org.springframework.batch.item.*` - Item processing interfaces and implementations
- `org.springframework.batch.repeat.*` - Repetition framework with completion policies and exception handling
- `org.springframework.batch.support.*` - Transaction support and utilities
- `org.springframework.batch.item.database.*` - JDBC, JPA, MongoDB readers/writers
- `org.springframework.batch.item.file.*` - Flat file, XML processors
- `org.springframework.batch.item.json.*` - JSON readers/writers
- `org.springframework.batch.item.kafka.*` - Kafka integration
- `org.springframework.batch.item.mail.*` - Email support

### Testing Strategy
- **Unit Tests**: Named `*Tests.java`, run with `mvn test`
- **Integration Tests**: Named `*IntegrationTests.java`, require Docker, excluded from basic test run
- **Functional Tests**: Named `*FunctionalTests.java`, similar to integration tests
- Test databases: H2 (in-memory), HSQL, PostgreSQL, MySQL, Oracle, SQL Server (via TestContainers)

### Development Conventions
- Java 17+ required
- Spring Java Format enforced (run `./mvnw spring-javaformat:apply` before committing)
- All public classes need proper JavaDoc
- Use AssertJ for test assertions
- Follow existing patterns for new ItemReader/ItemWriter implementations