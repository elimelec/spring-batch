package org.springframework.batch.samples.profiling.writer;

public enum StorageType {

	BLACKHOLE, // No storage (fastest, for pure processing tests)
	MEMORY, // In-memory list
	HSQLDB, // HSQLDB embedded database
	MYSQL, // MySQL database
	POSTGRES, // PostgreSQL database
	MONGODB, // MongoDB (NoSQL)
	FILE // File-based storage

}