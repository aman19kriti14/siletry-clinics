package com.gatistack.siletry.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

	private final SchemaMultiTenantConnectionProvider connectionProvider;
	private final TenantIdentifierResolver tenantIdentifierResolver;

	public JpaConfig(SchemaMultiTenantConnectionProvider connectionProvider,
			TenantIdentifierResolver tenantIdentifierResolver) {
		this.connectionProvider = connectionProvider;
		this.tenantIdentifierResolver = tenantIdentifierResolver;
	}

	@Bean
	public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
		return props -> {
			props.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
			props.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
		};
	}
}
