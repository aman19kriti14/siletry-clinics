package com.gatistack.siletry.security;

import com.gatistack.siletry.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String header = request.getHeader("Authorization");
			if (header != null && header.startsWith("Bearer ")) {
				String token = header.substring(7);
				if (jwtService.isValid(token)) {
					String staffUserId = jwtService.extractStaffUserId(token);
					String schema = jwtService.extractSchema(token);

					TenantContext.setSchema(schema);

					var authentication = new UsernamePasswordAuthenticationToken(staffUserId, null, List.of());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
			filterChain.doFilter(request, response);
		} finally {
			TenantContext.clear(); // unchanged from #2 - still the critical cleanup
		}
	}
}