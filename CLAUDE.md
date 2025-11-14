# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

### Essential Commands
- **Build project:** `./mvnw package`
- **Run tests:** `./mvnw test`
- **Run specific test:** `./mvnw test -Dtest=ClassName#methodName`
- **Run integration tests:** `./mvnw verify` (requires Docker)
- **Skip tests:** `./mvnw package -DskipTests`
- **Format code:** `./mvnw spring-javaformat:apply`
- **Check formatting:** `./mvnw spring-javaformat:check`
- **Generate JavaDocs:** `./mvnw javadoc:aggregate`
- **Generate documentation:** `cd spring-batch-docs && ../mvnw antora:antora`

### Testing Strategy
- Unit tests: Standard `*Test.java` files
- Integration tests: `*IntegrationTests.java` or `*FunctionalTests.java` (excluded from regular test run)
- Test annotation: Use `@SpringBatchTest` for batch-specific test setup
- Database tests: H2 is default for tests, other databases require Docker
- Clean tests: `./mvnw clean test` (ensures fresh build)
- Specific module tests: `./mvnw test -pl spring-batch-core`

## High-Level Architecture

### Module Structure
Spring Batch is organized into 7 modules:

1. **spring-batch-infrastructure** - Core item processing infrastructure
   - ItemReaders, ItemWriters, ItemProcessors
   - Support for files (XML, JSON, CSV), databases, messaging systems
   - Package: `org.springframework.batch.item.*`

2. **spring-batch-core** - Core batch domain and execution
   - Job, Step, Chunk abstractions
   - JobRepository, JobLauncher, StepExecutionListener
   - Package: `org.springframework.batch.core.*`

3. **spring-batch-test** - Testing support and utilities
   - `@SpringBatchTest` annotation for test configuration
   - JobLauncherTestUtils, JobRepositoryTestUtils

4. **spring-batch-integration** - Spring Integration support
   - Remote partitioning, chunking
   - Message-based job launching

5. **spring-batch-samples** - Example implementations

6. **spring-batch-docs** - Antora-based documentation

7. **spring-batch-bom** - Bill of Materials for dependency management

### Core Concepts

**Job/Step/Chunk Pattern:**
- Jobs contain Steps
- Steps can be chunk-oriented (read-process-write pattern) or tasklet-based
- Chunks process items in configurable batch sizes with transaction boundaries

**Key Classes:**
- `Job` - Top-level batch job
- `Step` - Individual processing step within a job
- `ItemReader<T>` - Reads input data
- `ItemProcessor<I,O>` - Processes/transforms items
- `ItemWriter<T>` - Writes output data
- `JobRepository` - Persists job metadata
- `JobLauncher` - Launches job executions

**Configuration Patterns:**
- Use `@EnableBatchProcessing` for batch configuration
- Jobs and steps configured as Spring beans
- Chunk-oriented steps use `.chunk(chunkSize)` builder pattern

## Development Guidelines

### Code Style
- Spring Framework code style enforced via Spring Java Format plugin
- License headers required (Apache 2.0)
- JavaDoc required for public APIs with `@since` tags for new features

### Git Workflow
- Feature branches named after issues: `GH-<issue-number>`
- Sign-off commits with DCO (use `git commit -s`)
- Linear history required (no merge commits in PRs)
- Squash commits before merging if needed

### Testing Requirements
- All new features must include tests
- Integration tests for database/external system interactions
- Use test utilities from spring-batch-test module
- Micrometer observability tests for new metrics

### Common Development Patterns

**Creating a Simple Job:**
```java
@Bean
public Job myJob(JobRepository jobRepository, Step step1) {
    return new JobBuilder("myJob", jobRepository)
        .start(step1)
        .build();
}

@Bean
public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("step1", jobRepository)
        .<Input, Output>chunk(10, transactionManager)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .build();
}
```

**Custom ItemReader/Writer:**
- Extend `AbstractItemCountingItemStreamItemReader` for readers
- Implement `ItemWriter<T>` interface for writers
- Ensure proper resource management in `open()`, `close()` methods

### Database Support
- JDBC repositories support: H2, HSQLDB, PostgreSQL, MySQL, Oracle, SQL Server, DB2, Derby, SQLite, Sybase
- Schema scripts in `spring-batch-core/src/main/resources/org/springframework/batch/core/schema-*.sql`
- Use `@BatchDataSource` to designate batch metadata datasource
- Test with specific database: Set spring.profiles.active (e.g., `./mvnw test -Dspring.profiles.active=mysql`)

### Observability
- Micrometer integration for metrics
- JFR (Java Flight Recorder) support for profiling
- Key metrics: job duration, step execution time, read/write rates
- ObservationRegistry required for metrics (falls back to NOOP if not configured)

### Dependency Injection Patterns
- Configuration classes use `@Configuration` and `@Bean` annotations
- JobRepository and PlatformTransactionManager are typically autowired
- Use builder pattern for Job and Step creation (JobBuilder, StepBuilder)
- Batch infrastructure beans auto-configured with `@EnableBatchProcessing`