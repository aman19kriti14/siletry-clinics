package com.gatistack.siletry.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggerFilter implements Filter {

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;

		System.out.println(">>>> REQUEST: " + request.getMethod() + " " + request.getRequestURI());

		chain.doFilter(req, res);

		System.out.println("<<<< RESPONSE FINISHED");
	}
}
