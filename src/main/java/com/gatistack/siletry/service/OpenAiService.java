package com.gatistack.siletry.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

	private final RestClient restClient;
	private final String model;

	public OpenAiService(@Value("${openai.api.key}") String apiKey, @Value("${openai.api.model}") String model) {
		this.model = model;
		this.restClient = RestClient.builder().baseUrl("https://api.openai.com/v1")
				.defaultHeader("Authorization", "Bearer " + apiKey).build();
	}

	// Existing simple method - kept for the follow-up triage use case (no tools
	// needed there)
	public String chat(String systemPrompt, String userMessage) {
		Map<String, Object> requestBody = Map.of("model", model, "messages", List
				.of(Map.of("role", "system", "content", systemPrompt), Map.of("role", "user", "content", userMessage)),
				"temperature", 0.2);
		return extractContent(callApi(requestBody));
	}

	// New: supports tools (function calling) and full message history - returns the
	// raw response map so the caller can inspect tool_calls, not just text content
	public Map<String, Object> chatWithTools(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
		Map<String, Object> requestBody = Map.of("model", model, "messages", messages, "tools", tools, "tool_choice",
				"auto", "temperature", 0.3);
		return callApi(requestBody);
	}

	private Map<String, Object> callApi(Map<String, Object> requestBody) {
		return restClient.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).body(requestBody)
				.retrieve().body(Map.class);
	}

	@SuppressWarnings("unchecked")
	private String extractContent(Map<String, Object> response) {
		List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
		Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
		return (String) message.get("content");
	}
}