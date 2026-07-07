package com.gatistack.siletry.service;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.Tenant;
import com.gatistack.siletry.repository.TenantRepository;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@Service
public class TenantProvisioningService {

	private static final Logger log = LoggerFactory.getLogger(TenantProvisioningService.class);
	private static final String TENANT_CHANGELOG = "db/migration/tenant/changelog-tenant.xml";

	private final TenantRepository tenantRepository;
	private final DataSource dataSource;
	private final PasswordEncoder passwordEncoder;

	public TenantProvisioningService(TenantRepository tenantRepository, DataSource dataSource,
			PasswordEncoder passwordEncoder) {
		this.tenantRepository = tenantRepository;
		this.dataSource = dataSource;
		this.passwordEncoder = passwordEncoder;
	}

	public Tenant provision(String clinicName, String schemaName, String address, String phone, String email,
			String ownerEmail, String ownerPassword, String ownerName) {
		validateSchemaName(schemaName);

		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
			}
			connection.setSchema(schemaName);
			Database database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(new JdbcConnection(connection));
			database.setDefaultSchemaName(schemaName);

			try (Liquibase liquibase = new Liquibase(TENANT_CHANGELOG, new ClassLoaderResourceAccessor(), database)) {
				liquibase.update("");
			}
			// connection is now closed by Liquibase - do NOT reuse it below

		} catch (Exception e) {
			log.error("Failed to run schema migration for tenant: {}", schemaName, e);
			throw new IllegalStateException("Failed to provision tenant schema: " + schemaName, e);
		}

		// Fresh connection for the owner seed insert, since Liquibase closed the
		// previous one
		try (Connection connection = dataSource.getConnection()) {
			connection.setSchema(schemaName);
			try (var stmt = connection
					.prepareStatement("INSERT INTO staff_user (id, email, password_hash, name, role, created_at) "
							+ "VALUES (?, ?, ?, ?, 'OWNER', now())")) {
				stmt.setString(1, java.util.UUID.randomUUID().toString());
				stmt.setString(2, ownerEmail);
				stmt.setString(3, passwordEncoder.encode(ownerPassword));
				stmt.setString(4, ownerName);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Failed to seed owner account for tenant: {}", schemaName, e);
			throw new IllegalStateException("Failed to provision tenant schema: " + schemaName, e);
		}

		Tenant tenant = new Tenant();
		tenant.setClinicName(clinicName);
		tenant.setSchemaName(schemaName);
		tenant.setAddress(address);
		tenant.setPhone(phone);
		tenant.setEmail(email);
		tenant.setStatus(Tenant.TenantStatus.TRIAL);
		return tenantRepository.save(tenant);
	}

	private void validateSchemaName(String schemaName) {
		if (schemaName == null || !schemaName.matches("[a-z][a-z0-9_]{2,62}")) {
			throw new IllegalArgumentException(
					"Invalid schema name - must be lowercase, start with a letter, 3-63 chars: " + schemaName);
		}
	}
}