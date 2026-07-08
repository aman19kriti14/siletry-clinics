package com.gatistack.siletry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gatistack.siletry.entity.Tenant;
import com.gatistack.siletry.service.TenantProvisioningService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

	private final TenantProvisioningService provisioningService;

	public TenantController(TenantProvisioningService provisioningService) {
		this.provisioningService = provisioningService;
	}

	public record ProvisionRequest(@NotBlank String clinicName, @NotBlank String schemaName, String address,
			String phone, String email, @NotBlank String ownerEmail, @NotBlank String ownerPassword,
			@NotBlank String ownerName) {
	}

	@PostMapping("/provision")
	public Tenant provision(@RequestBody ProvisionRequest request) {
		return provisioningService.provision(request.clinicName(), request.schemaName(), request.address(),
				request.phone(), request.email(), request.ownerEmail(), request.ownerPassword(), request.ownerName());
	}

	// New public signup endpoint - no admin secret required
	@PostMapping("/signup")
	public Tenant signup(@RequestBody ProvisionRequest request) {
		return provisioningService.provision(request.clinicName(), request.schemaName(), request.address(),
				request.phone(), request.email(), request.ownerEmail(), request.ownerPassword(), request.ownerName());
	}

	@GetMapping("/api/ping")
	public String ping() {
		return "pong";
	}
}