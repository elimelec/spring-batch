# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Spring Batch BOM (Bill of Materials) module, part of the Spring Batch framework. Spring Batch is a comprehensive batch framework for enterprise systems, built on Spring Framework principles.

The BOM module provides centralized dependency management for all Spring Batch modules:
- `spring-batch-core` - Core batch framework functionality
- `spring-batch-infrastructure` - Infrastructure components
- `spring-batch-integration` - Spring Integration support
- `spring-batch-test` - Testing utilities

## Build Commands

### Build and Package
```bash
# From parent directory
cd ..
./mvnw package                 # Build and package without tests
./mvnw verify                  # Full build with all integration tests (requires Docker)
./mvnw clean install          # Clean, build, and install to local repository
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run integration tests (requires Docker)
./mvnw verify

# Run specific test
./mvnw test -Dtest=TestClassName

# Skip tests
./mvnw package -DskipTests
```

### Code Quality
```bash
# Format code (Spring Java Format)
./mvnw spring-javaformat:apply

# Check code format
./mvnw spring-javaformat:validate
```

## Architecture

Spring Batch follows a layered architecture:

1. **Infrastructure Layer** (`spring-batch-infrastructure`): Low-level components for item reading/writing, retry logic, and utilities
2. **Core Layer** (`spring-batch-core`): Job, Step, and batch execution management
3. **Integration Layer** (`spring-batch-integration`): Integration with Spring Integration for async/distributed processing
4. **Test Layer** (`spring-batch-test`): Testing support and utilities

Key concepts:
- **Job**: A batch process composed of one or more Steps
- **Step**: A phase in a Job, typically involving reading, processing, and writing data
- **JobRepository**: Persistence mechanism for Job metadata
- **JobLauncher**: Interface for launching Jobs
- **ItemReader/ItemProcessor/ItemWriter**: Core interfaces for chunk-oriented processing

## Development Notes

- Java 17+ required
- Maven build system (use `./mvnw` wrapper)
- Spring Framework 7.0.0-SNAPSHOT (development version)
- Follows Spring Java Format for code style
- All commits require DCO sign-off (`git commit -s`)
- Integration tests may require Docker running