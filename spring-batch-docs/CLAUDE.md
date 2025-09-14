# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is the Spring Batch documentation module, part of the larger Spring Batch project. The documentation is built using Antora and written in AsciiDoc format. Spring Batch is a comprehensive batch framework designed for enterprise batch job processing.

## Project Structure

- **Parent Project**: Located at `..` (spring-batch root)
- **Documentation Source**: `modules/ROOT/pages/` - Contains all AsciiDoc documentation files
- **Build Configuration**: Uses Maven with Antora plugin for documentation generation
- **Main Modules**: 
  - spring-batch-core - Core framework classes
  - spring-batch-infrastructure - Infrastructure components
  - spring-batch-test - Testing utilities
  - spring-batch-integration - Spring Integration support
  - spring-batch-samples - Example implementations

## Common Commands

### Build Documentation
```bash
# From spring-batch-docs directory
../mvnw antora:antora

# The generated documentation will be in target/antora/site/
```

### Build Entire Project
```bash
# From project root
./mvnw clean package

# Run with all integration tests (requires Docker)
./mvnw verify

# Run specific module tests
./mvnw test -pl spring-batch-core

# Skip tests for faster builds
./mvnw package -DskipTests
```

### Development Commands
```bash
# Install dependencies
./mvnw install

# Run a single test class
./mvnw test -Dtest=YourTestClass

# Run tests matching a pattern
./mvnw test -Dtest="*IntegrationTest"
```

## Documentation Architecture

The documentation uses Antora, a multi-repository documentation site generator:

- **antora.yml**: Component descriptor defining the documentation component
- **antora-playbook.yml**: Playbook for building documentation locally
- **modules/ROOT/nav.adoc**: Navigation structure for the documentation
- **modules/ROOT/pages/**: Individual documentation pages in AsciiDoc format

Key documentation sections:
- **Domain Model**: Core concepts like Job, Step, JobRepository
- **Job Configuration**: How to configure batch jobs
- **Step Processing**: Chunk-oriented processing, tasklets, flow control
- **Readers/Writers**: Item readers, writers, and processors
- **Scalability**: Parallel processing, partitioning, remote chunking
- **Testing**: Unit and integration testing approaches
- **Integration**: Spring Integration support

## Spring Batch Core Architecture

Spring Batch follows a layered architecture:

1. **Application Layer**: Business logic, jobs, steps
2. **Core Layer**: Job launching, step execution, job repository
3. **Infrastructure Layer**: Readers, writers, retry, repeat templates

Key Components:
- **Job**: Encapsulates an entire batch process
- **Step**: A phase in a job, contains ItemReader, ItemProcessor, ItemWriter
- **JobRepository**: Persistence mechanism for job metadata
- **JobLauncher**: Simple interface for launching jobs
- **ItemReader/Writer/Processor**: Interfaces for chunk-oriented processing

## Working with Documentation

When modifying documentation:
1. Edit AsciiDoc files in `modules/ROOT/pages/`
2. Update navigation in `modules/ROOT/nav.adoc` if adding new pages
3. Build locally with `../mvnw antora:antora` to verify changes
4. Documentation follows Spring documentation standards

## Maven Configuration

The project uses Maven for build management:
- Parent POM defines common dependencies and plugin versions
- Each module has its own POM inheriting from parent
- Documentation module uses `antora-maven-plugin` for building docs
- Version is managed at parent level (currently 6.0.0-SNAPSHOT)