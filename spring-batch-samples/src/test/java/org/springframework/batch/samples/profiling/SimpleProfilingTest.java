package org.springframework.batch.samples.profiling;

import org.junit.jupiter.api.Test;
import org.springframework.batch.samples.profiling.domain.DataRecord;
import org.springframework.batch.samples.profiling.processor.SimulatedWorkloadProcessor;
import org.springframework.batch.samples.profiling.processor.SimulatedWorkloadProcessor.WorkloadType;
import org.springframework.batch.samples.profiling.reader.DataGeneratorItemReader;
import org.springframework.batch.samples.profiling.writer.BlackHoleItemWriter;
import org.springframework.batch.item.Chunk;

import java.time.Duration;
import java.time.Instant;

/**
 * Simple profiling test that doesn't require Spring Batch framework Run with: mvn test
 * -Dtest=SimpleProfilingTest
 */
public class SimpleProfilingTest {

	@Test
	public void testMassiveDataProcessing() throws Exception {
		long totalRecords = Long.parseLong(System.getProperty("profiling.total.records", "10000"));
		int chunkSize = Integer.parseInt(System.getProperty("profiling.chunk.size", "1000"));
		WorkloadType workloadType = WorkloadType.valueOf(System.getProperty("profiling.workload.type", "MODERATE"));

		System.out.println("\n========================================");
		System.out.println("MASSIVE DATA PROFILING TEST");
		System.out.println("========================================");
		System.out.println("Total Records: " + totalRecords);
		System.out.println("Chunk Size: " + chunkSize);
		System.out.println("Workload Type: " + workloadType);
		System.out.println("========================================\n");

		DataGeneratorItemReader reader = new DataGeneratorItemReader(totalRecords);
		SimulatedWorkloadProcessor processor = new SimulatedWorkloadProcessor(workloadType);
		BlackHoleItemWriter writer = new BlackHoleItemWriter(true, 0);

		reader.open(new org.springframework.batch.item.ExecutionContext());

		Instant startTime = Instant.now();
		long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		long itemsProcessed = 0;
		int chunksProcessed = 0;

		try {
			while (true) {
				// Read a chunk
				Chunk<DataRecord> chunk = new Chunk<>();
				for (int i = 0; i < chunkSize; i++) {
					DataRecord item = reader.read();
					if (item == null) {
						break;
					}
					DataRecord processed = processor.process(item);
					if (processed != null) {
						chunk.add(processed);
					}
				}

				if (chunk.isEmpty()) {
					break;
				}

				// Write the chunk
				writer.write(chunk);

				itemsProcessed += chunk.size();
				chunksProcessed++;

				if (chunksProcessed % 10 == 0) {
					System.out.println("Processed " + itemsProcessed + " items in " + chunksProcessed + " chunks");
				}
			}
		}
		finally {
			reader.close();
		}

		Duration duration = Duration.between(startTime, Instant.now());
		long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long memoryUsed = endMemory - startMemory;

		System.out.println("\n========================================");
		System.out.println("RESULTS");
		System.out.println("========================================");
		System.out.println("Total Items Processed: " + itemsProcessed);
		System.out.println("Total Chunks: " + chunksProcessed);
		System.out.println("Duration: " + duration.toSeconds() + "." + (duration.toMillis() % 1000) + " seconds");
		System.out.println(
				"Throughput: " + String.format("%.2f items/second", itemsProcessed / (duration.toMillis() / 1000.0)));
		System.out
			.println("Avg time per item: " + String.format("%.3f ms", duration.toMillis() / (double) itemsProcessed));
		System.out.println("Memory Used: " + (memoryUsed / 1024 / 1024) + " MB");
		System.out.println("Items Written: " + writer.getItemsWritten());
		System.out.println("========================================\n");
	}

	@Test
	public void compareWorkloadTypes() throws Exception {
		long totalRecords = 1000;
		int chunkSize = 100;

		System.out.println("\n========================================");
		System.out.println("WORKLOAD TYPE COMPARISON");
		System.out.println("========================================\n");

		for (WorkloadType type : WorkloadType.values()) {
			System.out.println("Testing " + type + "...");

			DataGeneratorItemReader reader = new DataGeneratorItemReader(totalRecords);
			SimulatedWorkloadProcessor processor = new SimulatedWorkloadProcessor(type);
			BlackHoleItemWriter writer = new BlackHoleItemWriter(false, 0);

			reader.open(new org.springframework.batch.item.ExecutionContext());

			Instant startTime = Instant.now();

			while (true) {
				Chunk<DataRecord> chunk = new Chunk<>();
				for (int i = 0; i < chunkSize; i++) {
					DataRecord item = reader.read();
					if (item == null)
						break;
					DataRecord processed = processor.process(item);
					if (processed != null)
						chunk.add(processed);
				}
				if (chunk.isEmpty())
					break;
				writer.write(chunk);
			}

			reader.close();

			Duration duration = Duration.between(startTime, Instant.now());
			double throughput = totalRecords / (duration.toMillis() / 1000.0);

			System.out.println("  " + type + ": " + String.format("%.2f items/sec", throughput) + " ("
					+ duration.toMillis() + " ms total)");
		}

		System.out.println("\n========================================\n");
	}

}