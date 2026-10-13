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

import java.util.List;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.repository.explore.support.JdbcJobExplorerFactoryBean;
import org.springframework.batch.core.repository.support.JdbcJobRepositoryFactoryBean;
import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	private DataSource dataSource;

	private RowCountingJdbcTemplate jdbc;

	abstract DataSource createDataSource() throws Exception;

	abstract DatabaseType databaseType();

	@BeforeAll
	void setUpDatabase() throws Exception {
		this.dataSource = createDataSource();
		new ResourceDatabasePopulator(new ClassPathResource(databaseType().getProductSchema()))
			.execute(this.dataSource);
		this.jdbc = new RowCountingJdbcTemplate(this.dataSource);
		this.jdbc.batchUpdate("""
				INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
				VALUES (?, 0, 'someJob', ?)
				""", IntStream.rangeClosed(1, 10000).boxed().toList(), 1000, (ps, i) -> {
			ps.setLong(1, i * 3L);
			ps.setString(2, String.valueOf(i));
		});
		this.jdbc.update("""
				INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
				VALUES (1, 0, 'otherJob', 'other')
				""");
		this.jdbc.update("""
				INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
				VALUES (999999, 0, 'unrelated', 'unrelated')
				""");
		this.jdbc.execute("""
				CREATE TABLE CUSTOM_JOB_INSTANCE (
					JOB_INSTANCE_ID DECIMAL(19, 0) NOT NULL PRIMARY KEY,
					JOB_NAME VARCHAR(100) NOT NULL
				)
				""");
		this.jdbc.update("""
				INSERT INTO CUSTOM_JOB_INSTANCE SELECT JOB_INSTANCE_ID, JOB_NAME FROM BATCH_JOB_INSTANCE
				""");
	}

	@BeforeEach
	void resetCounts() {
		this.jdbc.resetCounts();
	}

	@ParameterizedTest
	@CsvSource({ "0,25", "1,25", "5000,25", "9998,25", "10010,25", "5000,0", "1,2147483647" })
	void nativePaginationMatchesPortablePagination(int start, int count) {
		List<JobInstance> page = dao().getJobInstances("someJob", start, count);
		int expectedSize = Math.min(Math.max(10000 - start, 0), count);
		assertEquals(expectedSize, page.size());
		assertEquals(expectedSize, this.jdbc.rowsVisited);
		assertEquals(count == 0 ? 0 : 1, this.jdbc.queryCount);
		assertEquals(IntStream.range(0, expectedSize).mapToObj(i -> (10000L - start - i) * 3).toList(), ids(page));

		JdbcJobInstanceDao portableDao = new JdbcJobInstanceDao(JdbcClient.create(this.jdbc));
		assertEquals(ids(portableDao.getJobInstances("someJob", start, count)), ids(page));
	}

	@Test
	void wildcardPaginatesAcrossMatchingJobNames() {
		List<JobInstance> page = dao().getJobInstances("*Job", 9999, 25);
		assertEquals(List.of(3L, 1L), ids(page));
		assertEquals(List.of("someJob", "otherJob"), page.stream().map(JobInstance::getJobName).toList());
		assertEquals(2, this.jdbc.rowsVisited);
	}

	@Test
	void missingJobReturnsEmptyPage() {
		assertTrue(dao().getJobInstances("missing", 0, 25).isEmpty());
		assertEquals(0, this.jdbc.rowsVisited);
	}

	@Test
	void originalConstructorRetainsPortablePagination() {
		JdbcJobInstanceDao portableDao = new JdbcJobInstanceDao(JdbcClient.create(this.jdbc));
		assertEquals(25, portableDao.getJobInstances("someJob", 5000, 25).size());
		assertEquals(5025, this.jdbc.rowsVisited);
	}

	@Test
	void honorsCustomTablePrefix() {
		JdbcJobInstanceDao dao = dao();
		dao.setTablePrefix("CUSTOM_");
		assertEquals(List.of(15000L, 14997L), ids(dao.getJobInstances("someJob", 5000, 2)));
		assertEquals(2, this.jdbc.rowsVisited);
	}

	@Test
	void unpagedMethodStillReturnsAllMatches() {
		assertEquals(10000, dao().getJobInstances("someJob").size());
	}

	@Test
	void repositoryFactoryAutoDetectionEnablesPagination() throws Exception {
		assertEquals(databaseType(), DatabaseType.fromMetaData(this.dataSource));
		JdbcJobRepositoryFactoryBean factory = new JdbcJobRepositoryFactoryBean();
		factory.setDataSource(this.dataSource);
		factory.setJdbcOperations(this.jdbc);
		factory.setTransactionManager(new JdbcTransactionManager(this.dataSource));
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
	}

	@Test
	void explorerFactoryAutoDetectionEnablesPagination() throws Exception {
		JdbcJobExplorerFactoryBean factory = new JdbcJobExplorerFactoryBean();
		factory.setDataSource(this.dataSource);
		factory.setJdbcOperations(this.jdbc);
		factory.setTransactionManager(new JdbcTransactionManager(this.dataSource));
		factory.afterPropertiesSet();
		assertEquals(25, factory.getObject().getJobInstances("someJob", 5000, 25).size());
		assertEquals(25, this.jdbc.rowsVisited);
	}

	private JdbcJobInstanceDao dao() {
		return new JdbcJobInstanceDao(JdbcClient.create(this.jdbc), databaseType().name());
	}

	private List<Long> ids(List<JobInstance> instances) {
		return instances.stream().map(JobInstance::getId).toList();
	}

}
