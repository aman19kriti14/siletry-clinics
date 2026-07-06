package com.gatistack.siletry.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class TenantStatementInspector implements StatementInspector {

	@Override
	public String inspect(String sql) {
		String schema = TenantContext.getSchema();
		if (schema == null) {
			return sql; // master-schema queries (Tenant table itself) pass through untouched
		}
		// Tenant-scoped entities are mapped without an explicit schema;
		// this prefixes their table refs with the resolved tenant schema at query time.
		return sql.replace("__TENANT_SCHEMA__", schema);
	}
}