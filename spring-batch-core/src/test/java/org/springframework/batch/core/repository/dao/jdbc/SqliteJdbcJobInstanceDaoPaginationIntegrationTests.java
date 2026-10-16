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

import java.nio.file.Path;

import javax.sql.DataSource;

import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;

import org.springframework.batch.infrastructure.support.DatabaseType;

class SqliteJdbcJobInstanceDaoPaginationIntegrationTests extends AbstractJdbcJobInstanceDaoPaginationIntegrationTests {

	@TempDir
	private static Path directory;

	@Override
	DataSource createDataSource() {
		SQLiteDataSource dataSource = new SQLiteDataSource();
		dataSource.setUrl("jdbc:sqlite:" + directory.resolve("spring-batch.sqlite"));
		return dataSource;
	}

	@Override
	DatabaseType databaseType() {
		return DatabaseType.SQLITE;
	}

}
