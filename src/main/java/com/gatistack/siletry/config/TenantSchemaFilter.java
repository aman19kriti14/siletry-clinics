package com.gatistack.siletry.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantSchemaFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getDetails() instanceof String schemaName) {
				// JWT auth populates schemaName as auth.details - wired in #6 (security)
				TenantContext.setSchema(schemaName);
			}
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear(); // critical: prevents one tenant's schema leaking into the next request
		}
	}
}