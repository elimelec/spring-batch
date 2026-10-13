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

import javax.sql.DataSource;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.batch.infrastructure.support.DatabaseType;

@Testcontainers(disabledWithoutDocker = true)
class SqlServerJdbcJobInstanceDaoPaginationIntegrationTests
		extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	@Container
	static final MSSQLServerContainer sqlServer = new MSSQLServerContainer(
			DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-CU14-ubuntu-22.04"))
		.acceptLicense();

	@Override
	DataSource createDataSource() {
		SQLServerDataSource dataSource = new SQLServerDataSource();
		dataSource.setURL(sqlServer.getJdbcUrl());
		dataSource.setUser(sqlServer.getUsername());
		dataSource.setPassword(sqlServer.getPassword());
		return dataSource;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.SQLSERVER;
	}

}
