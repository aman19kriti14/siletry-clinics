package com.gatistack.siletry.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

	private final DataSource dataSource;

	public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connection.close();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		Connection connection = getAnyConnection();
		try (Statement statement = connection.createStatement()) {
			statement.execute("SET search_path TO " + sanitize(tenantIdentifier) + ", public");
		}
		return connection;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("SET search_path TO public");
		} finally {
			connection.close();
		}
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return false;
	}

	@Override
	public boolean isUnwrappableAs(Class<?> unwrapType) {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return null;
	}

	// Schema name flows into a raw SQL statement (SET search_path can't be
	// parameterized) -
	// sanitize strictly to prevent SQL injection via a malicious/corrupted
	// schema_name value.
	private String sanitize(String schemaName) {
		if (schemaName == null || !schemaName.matches("[a-zA-Z0-9_]+")) {
			throw new IllegalArgumentException("Invalid schema name: " + schemaName);
		}
		return schemaName;
	}
}