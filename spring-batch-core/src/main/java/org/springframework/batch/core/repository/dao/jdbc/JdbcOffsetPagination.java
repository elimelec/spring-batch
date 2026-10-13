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

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * Internal SQL for offset-based repository queries. The caller supplies a deterministic
 * ORDER BY clause and binds the start and count parameters. A null query means that the
 * caller must use portable ResultSet pagination.
 */
final class JdbcOffsetPagination {

	private JdbcOffsetPagination() {
	}

	static @Nullable String queryFor(String query, @Nullable String databaseType) {
		if (databaseType == null) {
			return null;
		}
		return switch (databaseType.toUpperCase(Locale.ROOT)) {
			case "H2", "HSQL", "DERBY", "SQLSERVER", "DB2" -> query + " OFFSET :start ROWS FETCH NEXT :count ROWS ONLY";
			case "POSTGRES", "MYSQL", "MARIADB", "SQLITE" -> query + " LIMIT :count OFFSET :start";
			case "ORACLE" -> """
					SELECT JOB_INSTANCE_ID, JOB_NAME FROM (
						SELECT PAGE_.*, ROWNUM AS PAGE_ROW_NUMBER FROM (
							%s
						) PAGE_ WHERE ROWNUM <= :start + :count
					) WHERE PAGE_ROW_NUMBER > :start ORDER BY PAGE_ROW_NUMBER
					""".formatted(query);
			default -> null;
		};
	}

}
