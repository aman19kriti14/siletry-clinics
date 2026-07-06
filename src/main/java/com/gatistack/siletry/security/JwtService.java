package com.gatistack.siletry.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMs = expirationMs;
	}

	public String generateToken(String staffUserId, String tenantSchemaName) {
		return Jwts.builder().subject(staffUserId).claim("schema", tenantSchemaName).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	public String extractSchema(String token) {
		return parseClaims(token).get("schema", String.class);
	}

	public String extractStaffUserId(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}