package com.gatistack.siletry.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsAppMediaService {

	private final RestClient restClient;

	public WhatsAppMediaService(@Value("${whatsapp.api.access-token}") String accessToken,
			@Value("${whatsapp.api.base-url}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("Authorization", "Bearer " + accessToken)
				.build();
	}

	public byte[] downloadMedia(String mediaId) {
		// Step 1: resolve the media ID to an actual download URL
		Map<String, Object> mediaInfo = restClient.get().uri("/{mediaId}", mediaId).retrieve().body(Map.class);
		String url = (String) mediaInfo.get("url");

		// Step 2: download the actual audio bytes from that URL (still needs auth
		// header)
		return restClient.get().uri(url).retrieve().body(byte[].class);
	}
}