package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, String> {
	Optional<Tenant> findBySchemaName(String schemaName);

	Optional<Tenant> findByEmail(String email);
}