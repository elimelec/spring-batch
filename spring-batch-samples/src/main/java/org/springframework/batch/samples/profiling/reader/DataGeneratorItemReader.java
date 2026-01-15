package org.springframework.batch.samples.profiling.reader;

import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.batch.samples.profiling.domain.DataRecord;
import org.springframework.util.ClassUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ItemReader that generates synthetic data for performance testing. This reader can
 * generate millions of records without requiring any external data source.
 */
public class DataGeneratorItemReader extends AbstractItemCountingItemStreamItemReader<DataRecord> {

	private final long totalRecords;

	private final AtomicLong currentId = new AtomicLong(0);

	private final int delayMillis;

	private final boolean simulateSlowIO;

	private final int payloadSize;

	public DataGeneratorItemReader(long totalRecords) {
		this(totalRecords, 0, false, 1024);
	}

	public DataGeneratorItemReader(long totalRecords, int delayMillis, boolean simulateSlowIO, int payloadSize) {
		this.totalRecords = totalRecords;
		this.delayMillis = delayMillis;
		this.simulateSlowIO = simulateSlowIO;
		this.payloadSize = payloadSize;
		setName(ClassUtils.getShortName(getClass()));
	}

	@Override
	protected DataRecord doRead() throws Exception {
		long id = currentId.incrementAndGet();

		if (id > totalRecords) {
			return null;
		}

		// Simulate slow I/O if requested (for testing bottlenecks)
		if (simulateSlowIO && id % 1000 == 0) {
			Thread.sleep(10);
		}

		// Add configurable delay if specified
		if (delayMillis > 0) {
			Thread.sleep(delayMillis);
		}

		DataRecord record = new DataRecord(id);

		// Customize payload size for memory testing
		if (payloadSize != 1024) {
			record.setPayload(new byte[payloadSize]);
		}

		return record;
	}

	@Override
	protected void doOpen() throws Exception {
		currentId.set(0);
	}

	@Override
	protected void doClose() throws Exception {
		// Nothing to close
	}

	@Override
	public void update(org.springframework.batch.item.ExecutionContext executionContext) {
		super.update(executionContext);
		executionContext.putLong("current.id", currentId.get());
	}

	@Override
	public void open(org.springframework.batch.item.ExecutionContext executionContext) {
		super.open(executionContext);
		if (executionContext.containsKey("current.id")) {
			currentId.set(executionContext.getLong("current.id"));
		}
	}

}