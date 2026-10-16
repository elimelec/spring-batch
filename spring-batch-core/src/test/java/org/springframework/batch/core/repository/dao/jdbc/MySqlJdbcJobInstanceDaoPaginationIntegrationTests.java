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

import java.sql.SQLException;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
class MySqlJdbcJobInstanceDaoPaginationIntegrationTests extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	@Container
	static final MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:9.2.0"));

	@Override
	DataSource createDataSource() throws SQLException {
		return createDataSource(false);
	}

	private DataSource createDataSource(boolean useServerPrepStmts) throws SQLException {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setURL(mysql.getJdbcUrl());
		dataSource.setUser(mysql.getUsername());
		dataSource.setPassword(mysql.getPassword());
		dataSource.setUseSSL(false);
		dataSource.setRewriteBatchedStatements(true);
		dataSource.setUseServerPrepStmts(useServerPrepStmts);
		dataSource.setEmulateUnsupportedPstmts(false);
		return dataSource;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.MYSQL;
	}

	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void bindsLimitAndOffsetWithClientAndServerPreparedStatements(boolean useServerPrepStmts) throws SQLException {
		RowCountingJdbcTemplate jdbc = new RowCountingJdbcTemplate(createDataSource(useServerPrepStmts));
		JdbcJobInstanceDao dao = new JdbcJobInstanceDao(JdbcClient.create(jdbc), databaseType().name());
		var page = dao.getJobInstances("someJob", 5000, 25);
		assertEquals(25, page.size());
		assertEquals(15000L, page.get(0).getId());
		assertEquals(14928L, page.get(24).getId());
		assertEquals(25, jdbc.rowsVisited);
	}

}
