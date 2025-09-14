# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

This is a Maven-based Spring Batch Core project. Use the Maven wrapper for all commands:

- **Build**: `./mvnw package` (basic build)
- **Full Build with Integration Tests**: `./mvnw verify` (requires Docker)
- **Unit Tests Only**: `./mvnw test`
- **Integration Tests Only**: `./mvnw failsafe:integration-test`
- **Run Single Test**: `./mvnw test -Dtest=ClassName#methodName`
- **Code Formatting**: `./mvnw spring-javaformat:apply`
- **Format Validation**: `./mvnw validate`

## Architecture Overview

Spring Batch Core implements batch processing through a domain-driven architecture with these key concepts:

### Core Domain Model
- **Job**: Top-level batch process containing one or more Steps
- **Step**: Individual processing unit within a Job (chunk-oriented or tasklet-based)
- **JobExecution/StepExecution**: Runtime execution tracking with status and context
- **JobRepository**: Metadata persistence layer (JDBC or MongoDB)

### Processing Models
1. **Chunk-Oriented**: Read-Process-Write pattern with configurable chunk sizes
   - ItemReader → ItemProcessor (optional) → ItemWriter
   - Fault tolerance via retry/skip policies
   
2. **Tasklet**: Simple single-method execution for setup/cleanup tasks

### Key Packages
- `org.springframework.batch.core`: Domain objects, job/step definitions
- `org.springframework.batch.core.repository`: Metadata persistence
- `org.springframework.batch.core.launch`: Job launching infrastructure
- `org.springframework.batch.core.configuration`: Annotation-based configuration
- `org.springframework.batch.core.step.item`: Item processing components
- `org.springframework.batch.core.observability`: Micrometer integration

### Design Patterns
- **Builder Pattern**: JobBuilder, StepBuilder for fluent configuration
- **Template Method**: AbstractJob, AbstractStep provide common behavior
- **Repository Pattern**: JobRepository for metadata operations
- **Strategy Pattern**: Pluggable readers, writers, processors

## Development Guidelines

### Testing
- Unit tests: `*Tests.java`
- Integration tests: `*IntegrationTests.java` 
- Functional tests: `*FunctionalTests.java`
- Uses JUnit 5, Mockito, AssertJ, Testcontainers

### Code Style
- Spring Java Format enforced (run `./mvnw spring-javaformat:apply` before committing)
- Apache 2.0 license headers required
- JavaDoc for public APIs with `@since` tags for new additions

### Database Support
Schema scripts in `src/main/resources/org/springframework/batch/core/schema-*.sql` for:
HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server, DB2, H2, SQLite, Sybase

### Key Dependencies
- Spring Framework 7.0.0-SNAPSHOT
- Java 17 minimum
- Micrometer for observability
- Optional: Jackson, Spring Data (JPA/MongoDB), AspectJ