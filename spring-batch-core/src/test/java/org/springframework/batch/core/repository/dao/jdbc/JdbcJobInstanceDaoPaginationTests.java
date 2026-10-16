/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.core.repository.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.repository.explore.support.JdbcJobExplorerFactoryBean;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class JdbcJobInstanceDaoPaginationTests {

	private EmbeddedDatabase database;

	private RowCountingJdbcTemplate jdbc;

	@BeforeEach
	void setUp() {
		this.database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
			.setType(EmbeddedDatabaseType.H2)
			.addScript(DatabaseType.H2.getProductSchema())
			.build();
		this.jdbc = new RowCountingJdbcTemplate(this.database);
		this.jdbc.update("""
				INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
				SELECT X * 3, 0, 'someJob', CAST(X AS VARCHAR) FROM SYSTEM_RANGE(1, 10000)
				""");
		this.jdbc.update("""
				INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
				VALUES (999999, 0, 'unrelated', 'unrelated')
				""");
	}

	@AfterEach
	void tearDown() {
		this.database.shutdown();
	}

	// All executions use H2. LIMIT syntax checks here are not vendor integration tests.
	@ParameterizedTest
	@NullSource
	@ValueSource(
			strings = { "H2", "POSTGRES", "MYSQL", "MARIADB", "HSQL", "DERBY", "SQLITE", "SQLSERVER", "DB2", "CUSTOM" })
	void preservesPageContents(@Nullable String databaseType) {
		JdbcJobInstanceDao dao = dao(databaseType);
		assertEquals(List.of(30000L, 29997L), ids(dao.getJobInstances("someJob", 0, 2)));
		assertEquals(List.of(15000L, 14997L), ids(dao.getJobInstances("someJob", 5000, 2)));
		assertEquals(List.of(15000L, 14997L), ids(dao.getJobInstances("*Job", 5000, 2)));
		assertEquals(List.of(6L, 3L), ids(dao.getJobInstances("someJob", 9998, 25)));
		assertTrue(dao.getJobInstances("someJob", 10010, 25).isEmpty());
		assertTrue(dao.getJobInstances("missing", 0, 25).isEmpty());
		assertEquals(9999, dao.getJobInstances("someJob", 1, Integer.MAX_VALUE).size());
	}

	@Test
	void nativeQueryOnlyExposesRequestedRowsToJava() {
		assertEquals(25, dao("H2").getJobInstances("someJob", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
		assertEquals(1, this.jdbc.queryCount);
	}

	@Test
	void originalConstructorRetainsPortablePagination() {
		JdbcJobInstanceDao dao = new JdbcJobInstanceDao(JdbcClient.create(this.jdbc));
		assertEquals(25, dao.getJobInstances("someJob", 5000, 25).size());
		assertEquals(5025, this.jdbc.rowsVisited);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "H2", "POSTGRES" })
	void zeroCountDoesNotExecuteAQuery(@Nullable String databaseType) {
		assertTrue(dao(databaseType).getJobInstances("someJob", 5000, 0).isEmpty());
		assertEquals(0, this.jdbc.queryCount);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { "H2", "POSTGRES" })
	void honorsCustomTablePrefix(@Nullable String databaseType) {
		this.jdbc.execute("ALTER TABLE BATCH_JOB_INSTANCE RENAME TO CUSTOM_JOB_INSTANCE");
		JdbcJobInstanceDao dao = dao(databaseType);
		dao.setTablePrefix("CUSTOM_");
		assertEquals(List.of(15000L, 14997L), ids(dao.getJobInstances("someJob", 5000, 2)));
	}

	@Test
	void deprecatedFindMethodUsesNativePagination() {
		assertEquals(25, dao("H2").findJobInstancesByName("*Job", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
	}

	@Test
	void unpagedMethodStillReturnsAllMatches() {
		assertEquals(10000, dao("H2").getJobInstances("someJob").size());
	}

	@Test
	void repositoryFactoryAutoDetectionEnablesPagination() throws Exception {
		JdbcJobRepositoryFactoryBean factory = repositoryFactory();
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
	}

	@Test
	void repositoryFactoryHonorsExplicitDatabaseType() throws Exception {
		JdbcJobRepositoryFactoryBean factory = repositoryFactory();
		factory.setDatabaseType("DB2ZOS");
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(5025, this.jdbc.rowsVisited);
	}

	@Test
	void explorerFactoryAutoDetectionEnablesPagination() throws Exception {
		JdbcJobExplorerFactoryBean factory = explorerFactory(this.database);
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
	}

	@Test
	void explorerRetainsFallbackWhenMetadataIsUnavailable() throws Exception {
		DataSource unavailableMetadata = new DelegatingDataSource(this.database) {
			@Override
			public Connection getConnection() throws SQLException {
				throw new SQLException("Metadata connection unavailable");
			}
		};
		JdbcJobExplorerFactoryBean factory = explorerFactory(unavailableMetadata);
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(5025, this.jdbc.rowsVisited);
	}

	@Test
	void dialectSelectionIsExplicitAndUnknownTypesUseFallback() {
		String query = "SELECT JOB_INSTANCE_ID, JOB_NAME FROM BATCH_JOB_INSTANCE ORDER BY JOB_INSTANCE_ID DESC";
		for (String type : List.of("H2", "HSQL", "DERBY", "SQLSERVER", "DB2")) {
			assertEquals(query + " OFFSET :start ROWS FETCH NEXT :count ROWS ONLY",
					JdbcOffsetPagination.queryFor(query, type));
		}
		for (String type : List.of("POSTGRES", "MYSQL", "MARIADB", "SQLITE", "postgres")) {
			assertEquals(query + " LIMIT :count OFFSET :start", JdbcOffsetPagination.queryFor(query, type));
		}
		String oracle = JdbcOffsetPagination.queryFor("SELECT A, B FROM T ORDER BY A", "ORACLE");
		assertTrue(oracle.startsWith("SELECT * FROM ("));
		assertTrue(oracle.contains("SELECT A, B FROM T ORDER BY A"));
		for (DatabaseType type : DatabaseType.values()) {
			if (!List.of("H2", "HSQL", "DERBY", "SQLSERVER", "DB2", "POSTGRES", "MYSQL", "MARIADB", "SQLITE", "ORACLE")
				.contains(type.name())) {
				assertNull(JdbcOffsetPagination.queryFor(query, type.name()));
			}
		}
		assertNull(JdbcOffsetPagination.queryFor(query, null));
		assertNull(JdbcOffsetPagination.queryFor(query, "CUSTOM"));
	}

	private JdbcJobInstanceDao dao(@Nullable String databaseType) {
		return new JdbcJobInstanceDao(JdbcClient.create(this.jdbc), databaseType);
	}

	private JdbcJobRepositoryFactoryBean repositoryFactory() {
		JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
		factory.setDataSource(this.database);
		factory.setJdbcOperations(this.jdbc);
		factory.setTransactionManager(new JdbcTransactionManager(this.database));
		return factory;
	}

	private JdbcJobExplorerFactoryBean explorerFactory(DataSource dataSource) {
		JdbcJobExplorerFactoryBean factory = new JdbcJobExplorerFactoryBean();
		factory.setDataSource(dataSource);
		factory.setJdbcOperations(this.jdbc);
		factory.setTransactionManager(new JdbcTransactionManager(this.database));
		return factory;
	}

	private List<Long> ids(List<JobInstance> instances) {
		return instances.stream().map(JobInstance::getId).toList();
	}

}
