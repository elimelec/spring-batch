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

import com.ibm.db2.jcc.DB2SimpleDataSource;
import org.testcontainers.db2.Db2Container;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.batch.infrastructure.support.DatabaseType;

@Testcontainers(disabledWithoutDocker = true)
class Db2JdbcJobInstanceDaoPaginationIntegrationTests extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	@Container
	static final Db2Container db2 = new Db2Container(DockerImageName.parse("icr.io/db2_community/db2:12.1.0.0"))
		.acceptLicense();

	@Override
	DataSource createDataSource() {
		DB2SimpleDataSource dataSource = new DB2SimpleDataSource();
		dataSource.setDatabaseName(db2.getDatabaseName());
		dataSource.setUser(db2.getUsername());
		dataSource.setPassword(db2.getPassword());
		dataSource.setDriverType(4);
		dataSource.setServerName(db2.getHost());
		dataSource.setPortNumber(db2.getMappedPort(Db2Container.DB2_PORT));
		dataSource.setSslConnection(false);
		return dataSource;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.DB2;
	}

}
