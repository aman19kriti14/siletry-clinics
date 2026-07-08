package com.gatistack.siletry.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WhatsAppMessageSender {

	private final RestClient restClient;
	private final String phoneNumberId;

	public WhatsAppMessageSender(@Value("${whatsapp.api.access-token}") String accessToken,
			@Value("${whatsapp.api.phone-number-id}") String phoneNumberId,
			@Value("${whatsapp.api.base-url}") String baseUrl) {
		this.phoneNumberId = phoneNumberId;
		this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("Authorization", "Bearer " + accessToken)
				.build();
	}

	// For business-initiated messages (reminders, recall, follow-up) outside the
	// 24h session window - REQUIRES an approved template name from Meta's WhatsApp
	// Manager. This will fail with an error until such a template exists.
	public void sendTemplate(String toPhoneE164, String templateName, String languageCode, List<String> bodyParams) {
		Map<String, Object> component = Map.of("type", "body", "parameters",
				bodyParams.stream().map(p -> Map.of("type", "text", "text", p)).toList());

		Map<String, Object> requestBody = Map.of("messaging_product", "whatsapp", "to", toPhoneE164, "type", "template",
				"template", Map.of("name", templateName, "language", Map.of("code", languageCode), "components",
						List.of(component)));

		restClient.post().uri("/{phoneNumberId}/messages", phoneNumberId).contentType(MediaType.APPLICATION_JSON)
				.body(requestBody).retrieve().toBodilessEntity();
	}

	// For freeform replies WITHIN 24h of the patient's last message (e.g.
	// reception,
	// follow-up conversation replies) - no template needed here.
	public void sendText(String toPhoneE164, String text) {
		Map<String, Object> requestBody = Map.of("messaging_product", "whatsapp", "to", toPhoneE164, "type", "text",
				"text", Map.of("body", text));

		restClient.post().uri("/{phoneNumberId}/messages", phoneNumberId).contentType(MediaType.APPLICATION_JSON)
				.body(requestBody).retrieve().toBodilessEntity();
	}
}