package org.springframework.batch.samples.profiling.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.samples.profiling.domain.DataRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Processor that simulates various workloads for profiling.
 */
public class SimulatedWorkloadProcessor implements ItemProcessor<DataRecord, DataRecord> {

	public enum WorkloadType {

		NONE, // Pass-through
		LIGHT, // Simple transformations
		MODERATE, // Some computation
		HEAVY, // CPU intensive
		MEMORY, // Memory allocation intensive
		CRYPTO // Cryptographic operations

	}

	private final WorkloadType workloadType;

	private final AtomicLong processedCount = new AtomicLong(0);

	private MessageDigest md;

	public SimulatedWorkloadProcessor(WorkloadType workloadType) {
		this.workloadType = workloadType;
		if (workloadType == WorkloadType.CRYPTO) {
			try {
				this.md = MessageDigest.getInstance("SHA-256");
			}
			catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public DataRecord process(DataRecord item) throws Exception {
		if (item == null) {
			return null;
		}

		processedCount.incrementAndGet();

		switch (workloadType) {
			case NONE:
				return item;

			case LIGHT:
				return processLight(item);

			case MODERATE:
				return processModerate(item);

			case HEAVY:
				return processHeavy(item);

			case MEMORY:
				return processMemoryIntensive(item);

			case CRYPTO:
				return processCrypto(item);

			default:
				return item;
		}
	}

	private DataRecord processLight(DataRecord item) {
		item.setData(item.getData().toUpperCase());
		item.setAmount(item.getAmount().multiply(BigDecimal.valueOf(1.1)));
		item.setMetric1(item.getMetric1() * 2);
		return item;
	}

	private DataRecord processModerate(DataRecord item) {
		// String manipulations
		String data = item.getData();
		StringBuilder reversed = new StringBuilder(data).reverse();
		item.setData(reversed.toString());

		// Math operations
		for (int i = 0; i < 100; i++) {
			item.setMetric1(Math.sin(item.getMetric1()) * Math.cos(item.getMetric2()));
			item.setMetric2(Math.sqrt(Math.abs(item.getMetric2())) + Math.log(item.getMetric3() + 1));
		}

		// BigDecimal operations
		BigDecimal amount = item.getAmount();
		for (int i = 0; i < 10; i++) {
			amount = amount.multiply(BigDecimal.valueOf(1.0001)).setScale(4, RoundingMode.HALF_UP);
		}
		item.setAmount(amount);

		return item;
	}

	private DataRecord processHeavy(DataRecord item) {
		// Simulate CPU-intensive work
		long prime = findNthPrime(100);
		item.setMetric1((double) prime);

		// Complex string operations
		String data = item.getData();
		for (int i = 0; i < 50; i++) {
			data = data.replaceAll("[aeiou]", String.valueOf(i));
			data = new StringBuilder(data).reverse().toString();
		}
		item.setData(data.substring(0, Math.min(data.length(), 500)));

		// Nested loops
		double result = 0;
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				result += Math.sin(i) * Math.cos(j);
			}
		}
		item.setMetric2(result);

		return item;
	}

	private DataRecord processMemoryIntensive(DataRecord item) {
		// Create temporary objects
		String[] tempArray = new String[1000];
		for (int i = 0; i < tempArray.length; i++) {
			tempArray[i] = "Data-" + item.getId() + "-" + i;
		}

		// Concatenate strings (creates many temporary objects)
		String result = "";
		for (int i = 0; i < 100; i++) {
			result = result + item.getData() + "-" + i;
		}
		item.setData(result.substring(0, Math.min(result.length(), 500)));

		// Create and discard large byte arrays
		for (int i = 0; i < 10; i++) {
			byte[] temp = new byte[10240]; // 10KB
			temp[0] = (byte) item.getId().intValue();
		}

		return item;
	}

	private DataRecord processCrypto(DataRecord item) {
		// Hash the data
		byte[] hash = md.digest(item.getData().getBytes());
		item.setPayload(hash);

		// Hash multiple times
		byte[] data = item.getUuid().getBytes();
		for (int i = 0; i < 10; i++) {
			data = md.digest(data);
		}

		// Convert to hex string
		StringBuilder hexString = new StringBuilder();
		for (byte b : data) {
			hexString.append(String.format("%02x", b));
		}
		item.setDescription("Hash: " + hexString.toString());

		return item;
	}

	private long findNthPrime(int n) {
		int count = 0;
		long num = 2;
		while (count < n) {
			if (isPrime(num)) {
				count++;
			}
			if (count < n) {
				num++;
			}
		}
		return num;
	}

	private boolean isPrime(long n) {
		if (n <= 1)
			return false;
		if (n <= 3)
			return true;
		if (n % 2 == 0 || n % 3 == 0)
			return false;
		for (long i = 5; i * i <= n; i += 6) {
			if (n % i == 0 || n % (i + 2) == 0)
				return false;
		}
		return true;
	}

	public long getProcessedCount() {
		return processedCount.get();
	}

}