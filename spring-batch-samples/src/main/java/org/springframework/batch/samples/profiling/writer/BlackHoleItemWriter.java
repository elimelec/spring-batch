package org.springframework.batch.samples.profiling.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.samples.profiling.domain.DataRecord;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A writer that discards all data but tracks performance metrics. Useful for isolating
 * reader and processor performance from I/O.
 */
public class BlackHoleItemWriter implements ItemWriter<DataRecord> {

	private final AtomicLong itemsWritten = new AtomicLong(0);

	private final AtomicLong chunksWritten = new AtomicLong(0);

	private final AtomicLong totalBytes = new AtomicLong(0);

	private final boolean trackMetrics;

	private final int simulatedLatencyMs;

	public BlackHoleItemWriter() {
		this(true, 0);
	}

	public BlackHoleItemWriter(boolean trackMetrics, int simulatedLatencyMs) {
		this.trackMetrics = trackMetrics;
		this.simulatedLatencyMs = simulatedLatencyMs;
	}

	@Override
	public void write(Chunk<? extends DataRecord> items) throws Exception {
		if (simulatedLatencyMs > 0) {
			Thread.sleep(simulatedLatencyMs);
		}

		if (trackMetrics) {
			chunksWritten.incrementAndGet();
			for (DataRecord item : items) {
				itemsWritten.incrementAndGet();
				if (item.getPayload() != null) {
					totalBytes.addAndGet(item.getPayload().length);
				}
			}
		}
	}

	public long getItemsWritten() {
		return itemsWritten.get();
	}

	public long getChunksWritten() {
		return chunksWritten.get();
	}

	public long getTotalBytes() {
		return totalBytes.get();
	}

	public void resetMetrics() {
		itemsWritten.set(0);
		chunksWritten.set(0);
		totalBytes.set(0);
	}

}