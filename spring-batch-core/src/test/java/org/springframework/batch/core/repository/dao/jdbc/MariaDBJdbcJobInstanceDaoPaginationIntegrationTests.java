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

import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.batch.infrastructure.support.DatabaseType;

@Testcontainers(disabledWithoutDocker = true)
class MariaDBJdbcJobInstanceDaoPaginationIntegrationTests extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	@Container
	static final MariaDBContainer mariaDB = new MariaDBContainer(DockerImageName.parse("mariadb:11.8.2"));

	@Override
	DataSource createDataSource() throws SQLException {
		MariaDbDataSource dataSource = new MariaDbDataSource();
		dataSource.setUrl(mariaDB.getJdbcUrl());
		dataSource.setUser(mariaDB.getUsername());
		dataSource.setPassword(mariaDB.getPassword());
		return dataSource;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.MARIADB;
	}

}
