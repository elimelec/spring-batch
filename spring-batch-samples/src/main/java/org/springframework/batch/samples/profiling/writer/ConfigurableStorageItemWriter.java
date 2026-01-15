package org.springframework.batch.samples.profiling.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.samples.profiling.domain.DataRecord;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Configurable writer that supports multiple storage backends for performance testing.
 */
public class ConfigurableStorageItemWriter implements ItemStreamWriter<DataRecord> {

	private final StorageType storageType;

	private final ItemWriter<DataRecord> delegate;

	private final AtomicLong itemsWritten = new AtomicLong(0);

	private final AtomicLong writeTimeMs = new AtomicLong(0);

	// For in-memory storage
	private final ConcurrentLinkedQueue<DataRecord> memoryStore = new ConcurrentLinkedQueue<>();

	public ConfigurableStorageItemWriter(StorageType storageType) {
		this(storageType, null, null, null, null);
	}

	public ConfigurableStorageItemWriter(StorageType storageType, String jdbcUrl, String username, String password,
			String tableName) {
		this.storageType = storageType;
		this.delegate = createWriter(storageType, jdbcUrl, username, password, tableName);
	}

	private ItemWriter<DataRecord> createWriter(StorageType type, String jdbcUrl, String username, String password,
			String tableName) {
		switch (type) {
			case BLACKHOLE:
				return new BlackHoleItemWriter(true, 0);

			case MEMORY:
				return items -> memoryStore.addAll(items.getItems());

			case HSQLDB:
				return createJdbcWriter(createHsqldbDataSource(), tableName != null ? tableName : "data_record");

			case MYSQL:
				if (jdbcUrl == null) {
					jdbcUrl = "jdbc:mysql://localhost:3306/batch_profiling?serverTimezone=UTC";
				}
				return createJdbcWriter(
						createDataSource("com.mysql.cj.jdbc.Driver", jdbcUrl, username != null ? username : "root",
								password != null ? password : "password"),
						tableName != null ? tableName : "data_record");

			case POSTGRES:
				if (jdbcUrl == null) {
					jdbcUrl = "jdbc:postgresql://localhost:5432/batch_profiling";
				}
				return createJdbcWriter(
						createDataSource("org.postgresql.Driver", jdbcUrl, username != null ? username : "postgres",
								password != null ? password : "password"),
						tableName != null ? tableName : "data_record");

			case FILE:
				return createFileWriter();

			default:
				throw new IllegalArgumentException("Unsupported storage type: " + type);
		}
	}

	private DataSource createHsqldbDataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
			.addScript("classpath:org/springframework/batch/samples/profiling/schema-hsqldb.sql")
			.generateUniqueName(true)
			.build();
	}

	private DataSource createDataSource(String driverClassName, String url, String username, String password) {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		return dataSource;
	}

	private ItemWriter<DataRecord> createJdbcWriter(DataSource dataSource, String tableName) {
		// Create table if not exists
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String createTableSql = String.format("""
				CREATE TABLE IF NOT EXISTS %s (
					id BIGINT PRIMARY KEY,
					uuid VARCHAR(255),
					data VARCHAR(1000),
					category VARCHAR(100),
					amount DECIMAL(10,2),
					timestamp TIMESTAMP,
					status INT,
					description VARCHAR(500),
					metric1 DOUBLE,
					metric2 DOUBLE,
					metric3 DOUBLE
				)
				""", tableName);

		try {
			jdbcTemplate.execute(createTableSql);
		}
		catch (Exception e) {
			// Table might already exist, that's fine
		}

		String sql = String.format(
				"""
						INSERT INTO %s (id, uuid, data, category, amount, timestamp, status, description, metric1, metric2, metric3)
						VALUES (:id, :uuid, :data, :category, :amount, :timestamp, :status, :description, :metric1, :metric2, :metric3)
						""",
				tableName);

		JdbcBatchItemWriter<DataRecord> writer = new JdbcBatchItemWriterBuilder<DataRecord>().dataSource(dataSource)
			.sql(sql)
			.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
			.build();
		writer.afterPropertiesSet();
		return writer;
	}

	private ItemWriter<DataRecord> createFileWriter() {
		return new FlatFileItemWriterBuilder<DataRecord>().name("dataRecordFileWriter")
			.resource(new FileSystemResource("target/profiling-output.csv"))
			.delimited()
			.delimiter(",")
			.names("id", "uuid", "category", "amount", "status")
			.build();
	}

	@Override
	public void write(Chunk<? extends DataRecord> items) throws Exception {
		long startTime = System.currentTimeMillis();

		delegate.write(items);

		long duration = System.currentTimeMillis() - startTime;
		writeTimeMs.addAndGet(duration);
		itemsWritten.addAndGet(items.size());
	}

	public long getItemsWritten() {
		return itemsWritten.get();
	}

	public long getWriteTimeMs() {
		return writeTimeMs.get();
	}

	public double getAvgWriteTimeMs() {
		long items = itemsWritten.get();
		return items > 0 ? (double) writeTimeMs.get() / items : 0;
	}

	public int getMemoryStoreSize() {
		return memoryStore.size();
	}

	public List<DataRecord> getMemoryStoreSnapshot() {
		return new ArrayList<>(memoryStore);
	}

	public void clearMemoryStore() {
		memoryStore.clear();
	}

	public StorageType getStorageType() {
		return storageType;
	}

	@Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
		if (delegate instanceof ItemStreamWriter) {
			((ItemStreamWriter<DataRecord>) delegate).open(executionContext);
		}
	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		if (delegate instanceof ItemStreamWriter) {
			((ItemStreamWriter<DataRecord>) delegate).update(executionContext);
		}
	}

	@Override
	public void close() throws ItemStreamException {
		if (delegate instanceof ItemStreamWriter) {
			((ItemStreamWriter<DataRecord>) delegate).close();
		}
	}

}