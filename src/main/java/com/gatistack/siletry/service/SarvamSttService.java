package com.gatistack.siletry.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class SarvamSttService {

	private final RestClient restClient;

	public SarvamSttService(@Value("${sarvam.api.key}") String apiKey,
			@Value("${sarvam.api.base-url}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).defaultHeader("api-subscription-key", apiKey).build();
	}

	// audioBytes: raw audio file content (WAV/MP3/OGG etc - WhatsApp voice notes
	// are typically OGG/Opus)
	public String transcribe(byte[] audioBytes, String filename) {
		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("file", new org.springframework.core.io.ByteArrayResource(audioBytes) {
			@Override
			public String getFilename() {
				return filename;
			}
		});
		form.add("model", "saaras:v3");
		form.add("mode", "transcribe"); // native-language transcription, not translation to English

		Map<String, Object> response = restClient.post().uri("/speech-to-text")
				.contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().body(Map.class);

		return (String) response.get("transcript");
	}
}