package com.gatistack.siletry.security;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

	private static final int MAX_ATTEMPTS = 5;
	private static final long WINDOW_MS = 60_000; // 1 minute

	private final ConcurrentHashMap<String, Window> attempts = new ConcurrentHashMap<>();

	private static class Window {
		AtomicInteger count = new AtomicInteger(0);
		long windowStart = System.currentTimeMillis();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getRequestURI().equals("/api/auth/login") && request.getMethod().equals("POST")) {
			String ip = request.getRemoteAddr();
			Window window = attempts.computeIfAbsent(ip, k -> new Window());

			synchronized (window) {
				long now = System.currentTimeMillis();
				if (now - window.windowStart > WINDOW_MS) {
					window.windowStart = now;
					window.count.set(0);
				}
				if (window.count.incrementAndGet() > MAX_ATTEMPTS) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Too many login attempts - try again in a minute");
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}