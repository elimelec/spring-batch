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

import org.junit.jupiter.api.AfterAll;

import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@SuppressWarnings("removal")
class DerbyJdbcJobInstanceDaoPaginationIntegrationTests extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	private EmbeddedDatabase database;

	@Override
	DataSource createDataSource() {
		this.database = new EmbeddedDatabaseBuilder().generateUniqueName(true)
			.setType(EmbeddedDatabaseType.DERBY)
			.build();
		return this.database;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.DERBY;
	}

	@AfterAll
	void shutDown() {
		if (this.database != null) {
			this.database.shutdown();
		}
	}

}
