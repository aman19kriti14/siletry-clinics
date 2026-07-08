package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.PatientRepository;
import com.gatistack.siletry.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

	private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

	private final String verifyToken;
	private final PatientRepository patientRepository;
	private final AiReceptionistService aiReceptionistService;
	private final WhatsAppMessageSender messageSender;
	private final WhatsAppMediaService mediaService;
	private final SarvamSttService sarvamSttService;

	public WhatsAppWebhookController(@Value("${whatsapp.webhook.verify-token}") String verifyToken,
			PatientRepository patientRepository, AiReceptionistService aiReceptionistService,
			WhatsAppMessageSender messageSender, WhatsAppMediaService mediaService, SarvamSttService sarvamSttService) {
		this.verifyToken = verifyToken;
		this.patientRepository = patientRepository;
		this.aiReceptionistService = aiReceptionistService;
		this.messageSender = messageSender;
		this.mediaService = mediaService;
		this.sarvamSttService = sarvamSttService;
	}

	// Meta calls this once, when you configure the webhook URL in the dashboard,
	// to confirm you actually control this endpoint.
	@GetMapping
	public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
			@RequestParam("hub.verify_token") String token, @RequestParam("hub.challenge") String challenge) {

		if ("subscribe".equals(mode) && verifyToken.equals(token)) {
			return ResponseEntity.ok(challenge);
		}
		return ResponseEntity.status(403).body("Verification failed");
	}

	// Meta calls this for every inbound event (messages, delivery receipts, etc.)
	@PostMapping
	@SuppressWarnings("unchecked")
	public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
		try {
			processPayload(payload);
		} catch (Exception e) {
			// Never let a processing error cause Meta to see a failure and retry-storm us -
			// log it and still return 200, but the failure is visible in our own logs.
			log.error("Failed to process WhatsApp webhook payload", e);
		}
		return ResponseEntity.ok().build(); // Meta requires a fast 200 response regardless
	}

	@SuppressWarnings("unchecked")
	private void processPayload(Map<String, Object> payload) {
		List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
		if (entries == null)
			return;

		for (Map<String, Object> entry : entries) {
			List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
			if (changes == null)
				continue;

			for (Map<String, Object> change : changes) {
				Map<String, Object> value = (Map<String, Object>) change.get("value");
				List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
				if (messages == null)
					continue; // could be a status/delivery-receipt event, not a message

				for (Map<String, Object> message : messages) {
					handleInboundMessage(message);
				}
			}
		}
	}

	private void handleInboundMessage(Map<String, Object> message) {
		String fromPhone = "+" + message.get("from"); // Meta sends without leading '+'
		String type = (String) message.get("type");

		String textContent = extractTextContent(message, type);
		if (textContent == null) {
			log.warn("Unsupported WhatsApp message type: {}", type);
			return;
		}

		Patient patient = patientRepository.findByPhone(fromPhone).orElseGet(() -> createNewPatient(fromPhone));

		String reply = aiReceptionistService.handleMessage(patient, textContent);

		if (reply != null) {
			// Freeform text is fine here - this is a reply WITHIN the patient's own
			// 24h session window, no template needed (unlike outbound reminders).
			messageSender.sendText(fromPhone, reply);
		}
		// reply == null means a human already took over (STAFF mode) - nothing to send,
		// the message is just logged for staff to see and respond to manually.
	}

	@SuppressWarnings("unchecked")
	private String extractTextContent(Map<String, Object> message, String type) {
		if ("text".equals(type)) {
			Map<String, Object> text = (Map<String, Object>) message.get("text");
			return (String) text.get("body");
		}
		if ("audio".equals(type)) {
			Map<String, Object> audio = (Map<String, Object>) message.get("audio");
			String mediaId = (String) audio.get("id");
			byte[] audioBytes = mediaService.downloadMedia(mediaId);
			return sarvamSttService.transcribe(audioBytes, "voice_note.ogg");
		}
		return null; // images, documents, locations etc. not yet handled
	}

	private Patient createNewPatient(String phone) {
		Patient patient = new Patient();
		patient.setName("Unknown");
		patient.setPhone(phone);
		patient.setCreatedVia(Patient.CreatedVia.WHATSAPP_INBOUND);
		patient.setPreferredLanguage("en");
		patient.setChannelPreference(Patient.ChannelPreference.WHATSAPP); // fix: they reached out via WhatsApp, so
																			// that's their channel
		return patientRepository.save(patient);
	}
}