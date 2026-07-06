package com.gatistack.siletry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminEndpointFilter extends OncePerRequestFilter {

	private final String adminSecret;

	public AdminEndpointFilter(@Value("${admin.secret}") String adminSecret) {
		this.adminSecret = adminSecret;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getRequestURI().startsWith("/api/tenants/provision")) {
			String provided = request.getHeader("X-Admin-Secret");
			if (provided == null || !provided.equals(adminSecret)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid admin secret");
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
}