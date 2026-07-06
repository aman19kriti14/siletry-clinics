package com.gatistack.siletry.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

	private static final String DEFAULT_TENANT = "master";

	@Override
	public String resolveCurrentTenantIdentifier() {
		String schema = TenantContext.getSchema();
		return schema != null ? schema : DEFAULT_TENANT;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return true;
	}
}