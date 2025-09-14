# Massive Data Profiling Sample

This sample provides a comprehensive framework for profiling Spring Batch performance with massive datasets. Perfect for identifying performance bottlenecks, testing JVM optimizations, and analyzing batch processing behavior under load.

**NEW**: Now supports configurable storage backends including in-memory, HSQLDB, MySQL, PostgreSQL, and file-based storage!

## Project Overview

### Architecture

```
spring-batch-samples/src/main/java/org/springframework/batch/samples/profiling/
├── domain/
│   └── DataRecord.java                 # Domain object with realistic fields (1KB payload)
├── reader/
│   └── DataGeneratorItemReader.java    # Generates synthetic data on-the-fly
├── processor/
│   └── SimulatedWorkloadProcessor.java # 6 configurable workload types
├── writer/
│   └── BlackHoleItemWriter.java        # Fast writer that tracks metrics
└── test/
    └── SimpleProfilingTest.java        # Main test harness for profiling
```

### Key Features

- **Zero External Dependencies**: Synthetic data generation, no files needed
- **Configurable Workloads**: 6 types from NONE to HEAVY CPU/memory intensive
- **Real-time Metrics**: Throughput, memory usage, processing time
- **Profiler-Ready**: Designed for JFR, async-profiler, and other tools
- **Scalable**: Tested from 10 to 100M+ records

## Quick Start

### Basic Test Run (10K records)
```bash
cd spring-batch-samples
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000
```

**Expected Output:**
```
========================================
MASSIVE DATA PROFILING TEST
========================================
Total Records: 10000
Chunk Size: 1000
Workload Type: MODERATE
========================================
Processed 10000 items in 10 chunks

========================================
RESULTS
========================================
Total Items Processed: 10000
Total Chunks: 10
Duration: 0.162 seconds
Throughput: 61728.40 items/second
Avg time per item: 0.016 ms
Memory Used: 86 MB
========================================
```

### Good for Profiling (100K records, HEAVY workload)
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=100000 \
  -Dprofiling.workload.type=HEAVY
```
Takes ~12 seconds - perfect for attaching profilers!

### Compare Workload Types
```bash
../mvnw test -Dtest=SimpleProfilingTest#compareWorkloadTypes -q
```

**Output:**
```
WORKLOAD TYPE COMPARISON
========================================
Testing NONE...
  NONE: 18867.92 items/sec (53 ms total)
Testing LIGHT...
  LIGHT: 111111.11 items/sec (9 ms total)
Testing MODERATE...
  MODERATE: 71428.57 items/sec (14 ms total)
Testing HEAVY...
  HEAVY: 6172.84 items/sec (162 ms total)
Testing MEMORY...
  MEMORY: 8928.57 items/sec (112 ms total)
Testing CRYPTO...
  CRYPTO: 29411.76 items/sec (34 ms total)
```

## Configuration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `profiling.total.records` | 1000000 | Total number of records to process |
| `profiling.chunk.size` | 1000 | Records per chunk/transaction |
| `profiling.thread.count` | 4 | Number of threads for multi-threaded execution |
| `profiling.partition.count` | 8 | Number of partitions for partitioned execution |
| `profiling.workload.type` | MODERATE | Processing complexity (see below) |
| `profiling.storage.type` | BLACKHOLE | Storage backend (see below) |
| `profiling.jdbc.url` | - | JDBC URL for database storage |
| `profiling.jdbc.username` | - | Database username |
| `profiling.jdbc.password` | - | Database password |
| `profiling.table.name` | data_record | Target table name |
| `profiling.verbose` | false | Enable detailed chunk-level logging |

### Workload Types

- **NONE**: Pass-through (no processing) - ~19K items/sec
- **LIGHT**: Simple transformations - ~111K items/sec
- **MODERATE**: Moderate computation (default) - ~71K items/sec
- **HEAVY**: CPU-intensive operations (prime numbers) - ~6K items/sec
- **MEMORY**: Memory allocation intensive - ~9K items/sec
- **CRYPTO**: Cryptographic operations (SHA-256) - ~29K items/sec

### Storage Types

- **BLACKHOLE**: No storage, metrics only (default) - Fastest, no I/O
- **MEMORY**: In-memory ConcurrentLinkedQueue - Fast, limited by heap
- **HSQLDB**: Embedded HSQLDB database - Good for testing
- **MYSQL**: MySQL database - Production-like performance
- **POSTGRES**: PostgreSQL database - Production-like performance
- **FILE**: CSV file output - I/O bound testing

## Common Launch Configurations

### Small Dataset (Quick Test)
```bash
# 1,000 records - runs in ~50ms
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000
```

### Test with Different Storage Types
```bash
# In-memory storage
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000 \
  -Dprofiling.storage.type=MEMORY

# HSQLDB embedded database
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000 \
  -Dprofiling.storage.type=HSQLDB

# MySQL (requires running MySQL instance)
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000 \
  -Dprofiling.storage.type=MYSQL \
  -Dprofiling.jdbc.url="jdbc:mysql://localhost:3306/testdb" \
  -Dprofiling.jdbc.username=root \
  -Dprofiling.jdbc.password=password

# PostgreSQL (requires running PostgreSQL instance)
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000 \
  -Dprofiling.storage.type=POSTGRES \
  -Dprofiling.jdbc.url="jdbc:postgresql://localhost:5432/testdb" \
  -Dprofiling.jdbc.username=postgres \
  -Dprofiling.jdbc.password=password

# File output
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000 \
  -Dprofiling.storage.type=FILE
```

### Compare Storage Performance
```bash
# Run the storage comparison test
../mvnw test -Dtest=SimpleProfilingTest#compareStorageTypes -q
```

### Medium Dataset (Development)
```bash
# 100,000 records - runs in ~0.7 seconds with MODERATE workload
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=100000
```

### Large Dataset (Profiling)
```bash
# 1 million records with HEAVY workload - runs in ~2 minutes
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000000 \
  -Dprofiling.workload.type=HEAVY \
  -Dprofiling.chunk.size=5000
```

### Massive Dataset (Stress Testing)
```bash
# 10 million records - for serious performance testing
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=10000000 \
  -Dprofiling.chunk.size=10000 \
  -DargLine="-Xmx4g -Xms4g"
```

## Profiling with JFR (Java Flight Recorder)

### Start Recording During Test
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000000 \
  -Dprofiling.workload.type=HEAVY \
  -DargLine="-XX:StartFlightRecording=filename=profile.jfr,duration=120s,settings=profile"

### Continuous Recording
```bash
../mvnw test -Dtest=MassiveDataProfilingTests \
  -DargLine="-XX:StartFlightRecording=filename=profile.jfr,dumponexit=true"
```

### Analyze with JDK Mission Control
```bash
jmc profile.jfr
```

## Profiling with Async Profiler

```bash
# Download async-profiler
wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-linux-x64.tar.gz
tar -xzf async-profiler-2.9-linux-x64.tar.gz

# Run with profiling
../mvnw test -Dtest=MassiveDataProfilingTests \
  -DargLine="-agentpath:./async-profiler-2.9-linux-x64/lib/libasyncProfiler.so=start,event=cpu,file=profile.html"
```

## Memory Profiling

### With Native Memory Tracking
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000000 \
  -DargLine="-XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics"
```

### Heap Dump on OutOfMemory
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=50000000 \
  -DargLine="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heap.hprof -Xmx2g"
```

## Performance Tuning Examples

### Optimize for Throughput
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=5000000 \
  -Dprofiling.chunk.size=10000 \
  -Dprofiling.workload.type=LIGHT \
  -DargLine="-XX:+UseParallelGC -Xmx4g -Xms4g"
```

### Test Memory Pressure
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000000 \
  -Dprofiling.chunk.size=100 \
  -Dprofiling.workload.type=MEMORY \
  -DargLine="-Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### CPU Bottleneck Analysis
```bash
../mvnw test -Dtest=SimpleProfilingTest#testMassiveDataProcessing \
  -Dprofiling.total.records=1000000 \
  -Dprofiling.workload.type=HEAVY \
  -Dprofiling.chunk.size=5000
```

## Understanding the Output

The sample provides detailed metrics:

```
=== JOB COMPLETED: multiThreadedMassiveDataJob ===
Status: COMPLETED
Total Duration: 45.234 seconds
CPU Time: 178234 ms
Total Items Processed: 1000000
Total Chunks: 1000
Throughput: 22098.34 items/second
Avg time per item: 0.045 ms

Memory Statistics:
  Memory Growth: 234.56 MB
  Peak Memory: 567.89 MB
  Final Heap: 345.67 MB
  GC Count: 12
  GC Time: 234 ms
```

## Profiling Different Scenarios

### 1. Reader Bottleneck
Test with no processing to isolate reader performance:
```bash
../mvnw test -Dtest=MassiveDataProfilingTests \
  -Dprofiling.workload.type=NONE \
  -Dprofiling.chunk.size=10000
```

### 2. Processor Bottleneck
Test with heavy processing:
```bash
../mvnw test -Dtest=MassiveDataProfilingTests \
  -Dprofiling.workload.type=HEAVY \
  -Dprofiling.thread.count=8
```

### 3. Transaction Overhead
Test with small chunks:
```bash
../mvnw test -Dtest=MassiveDataProfilingTests \
  -Dprofiling.chunk.size=10 \
  -Dprofiling.total.records=100000
```

### 4. Scaling Analysis
Compare different thread counts:
```bash
for threads in 1 2 4 8 16; do
  echo "Testing with $threads threads..."
  ../mvnw test -Dtest=MassiveDataProfilingTests#testMultiThreadedJob \
    -Dprofiling.thread.count=$threads \
    -Dprofiling.total.records=1000000
done
```

## Advanced Profiling

### Custom JVM Flags for Detailed Analysis
```bash
../mvnw test -Dtest=MassiveDataProfilingTests \
  -DargLine="-XX:+PrintGCDetails \
             -XX:+PrintGCTimeStamps \
             -XX:+PrintCompilation \
             -XX:+UnlockDiagnosticVMOptions \
             -XX:+PrintInlining \
             -Xlog:gc*:file=gc.log"
```

### With JMH Integration (if needed)
Add JMH dependency and create benchmark:
```java
@Benchmark
@BenchmarkMode(Mode.Throughput)
public void benchmarkChunkProcessing() {
    // Run a single chunk processing
}
```

## Troubleshooting

### OutOfMemoryError
- Reduce `profiling.total.records`
- Increase heap with `-Xmx4g`
- Reduce `profiling.chunk.size`
- Use `LIGHT` or `NONE` workload type

### Slow Performance
- Increase `profiling.thread.count`
- Increase `profiling.chunk.size`
- Use partitioned execution
- Profile with JFR to identify bottlenecks

### Inconsistent Results
- Warm up JVM first: Run test twice, measure second run
- Ensure no other processes consuming CPU
- Use `-Dprofiling.verbose=true` for detailed logging
- Disable CPU frequency scaling on Linux

## Extending the Sample

### Add Custom Workload
```java
// In SimulatedWorkloadProcessor.java
case CUSTOM:
    return processCustom(item);

private DataRecord processCustom(DataRecord item) {
    // Your custom processing logic
    return item;
}
```

### Add Database Writer
Replace `BlackHoleItemWriter` with `JdbcBatchItemWriter` in configuration.

### Add Real Data Source
Replace `DataGeneratorItemReader` with file/database readers.

## Performance Expectations

Typical throughput on modern hardware (4-core, 16GB RAM):

| Configuration | Records/Second |
|--------------|----------------|
| Single-threaded, LIGHT | 50,000-100,000 |
| Multi-threaded (4), MODERATE | 30,000-60,000 |
| Partitioned (8), MODERATE | 40,000-80,000 |
| Async, LIGHT | 60,000-120,000 |
| Single-threaded, HEAVY | 1,000-5,000 |

## Useful Commands Summary

```bash
# Quick test
../mvnw test -Dtest=MassiveDataProfilingTests#testSingleThreadedJob

# Full comparison
../mvnw test -Dtest=MassiveDataProfilingTests#compareAllStrategies

# Production-like load
../mvnw test -Dtest=MassiveDataProfilingTests \
  -Dprofiling.total.records=50000000 \
  -Dprofiling.chunk.size=5000 \
  -Dprofiling.thread.count=16

# With full profiling
../mvnw test -Dtest=MassiveDataProfilingTests \
  -DargLine="-XX:StartFlightRecording=filename=profile.jfr,dumponexit=true \
             -XX:+UnlockDiagnosticVMOptions \
             -XX:+DebugNonSafepoints"
```