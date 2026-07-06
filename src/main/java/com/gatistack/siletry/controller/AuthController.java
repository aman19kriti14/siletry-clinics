package com.gatistack.siletry.controller;

import com.gatistack.siletry.config.TenantContext;
import com.gatistack.siletry.entity.StaffUser;
import com.gatistack.siletry.entity.Tenant;
import com.gatistack.siletry.repository.StaffUserRepository;
import com.gatistack.siletry.repository.TenantRepository;
import com.gatistack.siletry.security.JwtService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final TenantRepository tenantRepository;
	private final StaffUserRepository staffUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(TenantRepository tenantRepository, StaffUserRepository staffUserRepository,
			PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.tenantRepository = tenantRepository;
		this.staffUserRepository = staffUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public record LoginRequest(@NotBlank String clinicSchema, @NotBlank String email, @NotBlank String password) {
	}

	public record LoginResponse(String token, String staffName, String role) {
	}

	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		// Runs against master schema - TenantContext is unset here (see #2 default
		// fallback)
		Tenant tenant = tenantRepository.findBySchemaName(request.clinicSchema())
				.orElseThrow(() -> new IllegalArgumentException("Unknown clinic"));

		try {
			TenantContext.setSchema(tenant.getSchemaName());

			StaffUser staffUser = staffUserRepository.findByEmail(request.email())
					.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

			if (!passwordEncoder.matches(request.password(), staffUser.getPasswordHash())) {
				throw new IllegalArgumentException("Invalid credentials");
			}

			String token = jwtService.generateToken(staffUser.getId(), tenant.getSchemaName());
			return new LoginResponse(token, staffUser.getName(), staffUser.getRole().name());
		} finally {
			TenantContext.clear();
		}
	}
}