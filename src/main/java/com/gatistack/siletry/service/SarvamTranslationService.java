package com.gatistack.siletry.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SarvamTranslationService {

	private final RestClient restClient;

	public SarvamTranslationService(@Value("${sarvam.api.key}") String apiKey,
			@Value("${sarvam.api.base-url}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("api-subscription-key", apiKey).build();
	}

	// Used only for the emergency-keyword safety check - translates whatever
	// language
	// the patient wrote in into English so our (English-only) keyword list can
	// catch it.
	public String translateToEnglish(String text) {
		Map<String, Object> requestBody = Map.of("input", text, "source_language_code", "auto", "target_language_code",
				"en-IN", "model", "mayura:v1");

		Map<String, Object> response = restClient.post().uri("/translate").contentType(MediaType.APPLICATION_JSON)
				.body(requestBody).retrieve().body(Map.class);

		return (String) response.get("translated_text");
	}
}