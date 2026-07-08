package com.gatistack.siletry.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final AdminEndpointFilter adminEndpointFilter;
	private final LoginRateLimitFilter loginRateLimitFilter;
	private final WhatsAppSignatureFilter whatsAppSignatureFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter, AdminEndpointFilter adminEndpointFilter,
			LoginRateLimitFilter loginRateLimitFilter, WhatsAppSignatureFilter whatsAppSignatureFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.adminEndpointFilter = adminEndpointFilter;
		this.loginRateLimitFilter = loginRateLimitFilter;
		this.whatsAppSignatureFilter = whatsAppSignatureFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> {
		}).csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/actuator/health").permitAll().requestMatchers("/api/tenants/signup")
						.permitAll().requestMatchers("/api/tenants/provision").permitAll()
						.requestMatchers("/api/whatsapp/webhook").permitAll().anyRequest().authenticated())
				.addFilterBefore(whatsAppSignatureFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(adminEndpointFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}