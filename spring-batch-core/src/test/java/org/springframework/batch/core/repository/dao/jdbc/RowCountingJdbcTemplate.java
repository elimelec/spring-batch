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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

final class RowCountingJdbcTemplate extends JdbcTemplate {

	int rowsVisited;

	int queryCount;

	RowCountingJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	void resetCounts() {
		this.rowsVisited = 0;
		this.queryCount = 0;
	}

	@Override
	public <T extends @Nullable Object> T query(PreparedStatementCreator creator, ResultSetExtractor<T> extractor) {
		this.queryCount++;
		return super.query(creator, rs -> {
			ResultSet tracked = (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] { ResultSet.class }, (proxy, method, args) -> {
						try {
							Object result = method.invoke(rs, args);
							if (method.getName().equals("next") && Boolean.TRUE.equals(result)) {
								this.rowsVisited++;
							}
							return result;
						}
						catch (InvocationTargetException ex) {
							throw ex.getCause();
						}
					});
			return extractor.extractData(tracked);
		});
	}

}
