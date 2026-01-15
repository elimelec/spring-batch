package org.springframework.batch.samples.profiling;

import org.junit.jupiter.api.Test;
import org.springframework.batch.samples.profiling.domain.DataRecord;
import org.springframework.batch.samples.profiling.processor.SimulatedWorkloadProcessor;
import org.springframework.batch.samples.profiling.processor.SimulatedWorkloadProcessor.WorkloadType;
import org.springframework.batch.samples.profiling.reader.DataGeneratorItemReader;
import org.springframework.batch.samples.profiling.writer.BlackHoleItemWriter;
import org.springframework.batch.samples.profiling.writer.ConfigurableStorageItemWriter;
import org.springframework.batch.samples.profiling.writer.StorageType;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

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
		StorageType storageType = StorageType.valueOf(System.getProperty("profiling.storage.type", "BLACKHOLE"));
		String jdbcUrl = System.getProperty("profiling.jdbc.url");
		String jdbcUsername = System.getProperty("profiling.jdbc.username");
		String jdbcPassword = System.getProperty("profiling.jdbc.password");
		String tableName = System.getProperty("profiling.table.name");

		System.out.println("\n========================================");
		System.out.println("MASSIVE DATA PROFILING TEST");
		System.out.println("========================================");
		System.out.println("Total Records: " + totalRecords);
		System.out.println("Chunk Size: " + chunkSize);
		System.out.println("Workload Type: " + workloadType);
		System.out.println("Storage Type: " + storageType);
		if (storageType == StorageType.MYSQL || storageType == StorageType.POSTGRES) {
			System.out.println("JDBC URL: " + (jdbcUrl != null ? jdbcUrl : "<default>"));
		}
		System.out.println("========================================\n");

		DataGeneratorItemReader reader = new DataGeneratorItemReader(totalRecords);
		SimulatedWorkloadProcessor processor = new SimulatedWorkloadProcessor(workloadType);
		ItemWriter<DataRecord> writer = createWriter(storageType, jdbcUrl, jdbcUsername, jdbcPassword, tableName);

		reader.open(new org.springframework.batch.item.ExecutionContext());
		if (writer instanceof ConfigurableStorageItemWriter) {
			((ConfigurableStorageItemWriter) writer).open(new org.springframework.batch.item.ExecutionContext());
		}

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
			if (writer instanceof ConfigurableStorageItemWriter) {
				((ConfigurableStorageItemWriter) writer).close();
			}
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
		if (writer instanceof BlackHoleItemWriter) {
			System.out.println("Items Written: " + ((BlackHoleItemWriter) writer).getItemsWritten());
		}
		else if (writer instanceof ConfigurableStorageItemWriter) {
			ConfigurableStorageItemWriter csw = (ConfigurableStorageItemWriter) writer;
			System.out.println("Items Written: " + csw.getItemsWritten());
			System.out.println("Avg Write Time: " + String.format("%.3f ms", csw.getAvgWriteTimeMs()));
			if (csw.getStorageType() == StorageType.MEMORY) {
				System.out.println("Memory Store Size: " + csw.getMemoryStoreSize());
			}
		}
		System.out.println("========================================\n");
	}

	private ItemWriter<DataRecord> createWriter(StorageType storageType, String jdbcUrl, String username,
			String password, String tableName) {
		if (storageType == StorageType.BLACKHOLE) {
			return new BlackHoleItemWriter(true, 0);
		}
		else {
			return new ConfigurableStorageItemWriter(storageType, jdbcUrl, username, password, tableName);
		}
	}

	@Test
	public void compareStorageTypes() throws Exception {
		long totalRecords = 1000;
		int chunkSize = 100;
		WorkloadType workloadType = WorkloadType.MODERATE;

		System.out.println("\n========================================");
		System.out.println("STORAGE TYPE COMPARISON");
		System.out.println("========================================\n");

		for (StorageType type : new StorageType[] { StorageType.BLACKHOLE, StorageType.MEMORY, StorageType.HSQLDB,
				StorageType.FILE }) {
			System.out.println("Testing " + type + "...");

			DataGeneratorItemReader reader = new DataGeneratorItemReader(totalRecords);
			SimulatedWorkloadProcessor processor = new SimulatedWorkloadProcessor(workloadType);
			ItemWriter<DataRecord> writer = createWriter(type, null, null, null, null);

			reader.open(new org.springframework.batch.item.ExecutionContext());
			if (writer instanceof ConfigurableStorageItemWriter) {
				((ConfigurableStorageItemWriter) writer).open(new org.springframework.batch.item.ExecutionContext());
			}

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
			if (writer instanceof ConfigurableStorageItemWriter) {
				((ConfigurableStorageItemWriter) writer).close();
			}

			Duration duration = Duration.between(startTime, Instant.now());
			double throughput = totalRecords / (duration.toMillis() / 1000.0);

			System.out.println("  " + type + ": " + String.format("%.2f items/sec", throughput) + " ("
					+ duration.toMillis() + " ms total)");
		}

		System.out.println("\n========================================\n");
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