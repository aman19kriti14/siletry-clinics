package com.gatistack.siletry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class WhatsAppSignatureFilter extends OncePerRequestFilter {

	private final String appSecret;

	public WhatsAppSignatureFilter(@Value("${whatsapp.api.app-secret}") String appSecret) {
		this.appSecret = appSecret;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!request.getRequestURI().equals("/api/whatsapp/webhook") || !"POST".equals(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		// Wrap so we can read the body for signature verification, then still let it
		// be read again downstream by the actual controller
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 10 * 1024); // 10KB -
																											// generous
																											// for
																											// WhatsApp's
																											// JSON
																											// webhook
																											// payloads
		wrappedRequest.getInputStream().readAllBytes(); // forces body into the cache

		String signatureHeader = request.getHeader("X-Hub-Signature-256");
		if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing signature");
			return;
		}

		byte[] body = wrappedRequest.getContentAsByteArray();
		String expectedSignature = "sha256=" + computeHmacSha256(body, appSecret);

		if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
				signatureHeader.getBytes(StandardCharsets.UTF_8))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid signature");
			return;
		}

		filterChain.doFilter(wrappedRequest, response);
	}

	private String computeHmacSha256(byte[] data, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] hash = mac.doFinal(data);
			StringBuilder hex = new StringBuilder();
			for (byte b : hash)
				hex.append(String.format("%02x", b));
			return hex.toString();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to compute HMAC signature", e);
		}
	}
}