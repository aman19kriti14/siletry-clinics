package com.gatistack.siletry.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.ConversationMessage;
import com.gatistack.siletry.entity.Escalation;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.ConversationMessageRepository;

@Service
public class AiReceptionistService {

	private static final Logger log = LoggerFactory.getLogger(AiReceptionistService.class);

	private static final List<String> EMERGENCY_KEYWORDS = List.of("chest pain", "can't breathe", "cannot breathe",
			"difficulty breathing", "heavy bleeding", "bleeding a lot", "unconscious", "suicide", "kill myself",
			"severe pain", "not breathing", "heart attack", "stroke");

	private static final String SYSTEM_PROMPT = """
			You are Siletry, the AI receptionist for this clinic, talking to a patient over WhatsApp.
			Be warm, brief, and clear - this is a chat conversation, not an email.
			Always reply in the same language the patient used to message you.

			You can look up doctors, check availability, book appointments, and reschedule
			existing appointments using the tools provided. Always confirm details (doctor,
			date, time) back to the patient before booking, unless they've already been
			explicit and unambiguous.

			If the patient describes a medical emergency or urgent symptom (severe pain,
			difficulty breathing, chest pain, heavy bleeding, etc.), do NOT attempt to book
			or troubleshoot - tell them clearly to call the clinic directly or go to the
			nearest emergency room immediately.

			If you're not confident you understood the request, ask a clarifying question
			rather than guessing.
			""";

	private final OpenAiService openAiService;
	private final AiFunctionExecutor functionExecutor;
	private final ConversationMessageRepository conversationMessageRepository;
	private final EscalationService escalationService;
	private final SarvamTranslationService sarvamTranslationService;

	public AiReceptionistService(OpenAiService openAiService, AiFunctionExecutor functionExecutor,
			ConversationMessageRepository conversationMessageRepository, EscalationService escalationService,
			SarvamTranslationService sarvamTranslationService) {
		this.openAiService = openAiService;
		this.functionExecutor = functionExecutor;
		this.conversationMessageRepository = conversationMessageRepository;
		this.escalationService = escalationService;
		this.sarvamTranslationService = sarvamTranslationService;
	}

	public String handleMessage(Patient patient, String inboundText) {
		saveMessage(patient, ConversationMessage.Direction.INBOUND, inboundText, null);

		// Hard-coded safety net - runs BEFORE the AI ever sees the message.
		// Translate to English first so this works regardless of what language the
		// patient actually wrote in - the keyword list itself stays English-only,
		// which is far easier to keep accurate than maintaining phrase lists per
		// language.
		String textForSafetyCheck;
		try {
			textForSafetyCheck = sarvamTranslationService.translateToEnglish(inboundText);
		} catch (Exception e) {
			// If translation fails for any reason, fail toward safety: check the raw
			// text as-is rather than skipping the check entirely.
			log.error("Translation failed during safety check, falling back to raw text", e);
			textForSafetyCheck = inboundText;
		}

		String lowerText = textForSafetyCheck.toLowerCase();
		boolean isEmergency = EMERGENCY_KEYWORDS.stream().anyMatch(lowerText::contains);
		if (isEmergency) {
			escalationService.escalate(patient, Escalation.Reason.EMERGENCY_KEYWORD, inboundText);
			String safetyReply = "This sounds urgent. Please call the clinic directly right now, "
					+ "or go to your nearest emergency room immediately. A staff member has been alerted.";
			saveMessage(patient, ConversationMessage.Direction.OUTBOUND, safetyReply, "ESCALATED: emergency keyword");
			return safetyReply;
		}

		if (patient.getConversationMode() == Patient.ConversationMode.STAFF) {
			return null;
		}

		List<Map<String, Object>> messages = buildMessageHistory(patient, inboundText);
		List<Map<String, Object>> tools = functionExecutor.getToolDefinitions();

		String actionLog = null;
		String finalReply;

		for (int turn = 0; turn < 5; turn++) {
			Map<String, Object> response = openAiService.chatWithTools(messages, tools);
			Map<String, Object> message = extractMessage(response);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");

			if (toolCalls == null || toolCalls.isEmpty()) {
				finalReply = (String) message.get("content");
				saveMessage(patient, ConversationMessage.Direction.OUTBOUND, finalReply, actionLog);
				return finalReply;
			}

			messages.add(message);
			for (Map<String, Object> toolCall : toolCalls) {
				String result = executeToolCall(toolCall, patient);
				actionLog = (actionLog == null ? "" : actionLog + "; ") + result;
				messages.add(toolResultMessage(toolCall, result));
			}
		}

		String fallback = "I'm having trouble completing that - let me get a staff member to help you.";
		saveMessage(patient, ConversationMessage.Direction.OUTBOUND, fallback, actionLog);
		return fallback;
	}

	@SuppressWarnings("unchecked")
	private String executeToolCall(Map<String, Object> toolCall, Patient patient) {
		Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
		String name = (String) function.get("name");
		String argsJson = (String) function.get("arguments");

		Map<String, Object> arguments;
		try {
			arguments = new com.fasterxml.jackson.databind.ObjectMapper().readValue(argsJson, Map.class);
		} catch (Exception e) {
			log.error("Failed to parse tool call arguments: {}", argsJson, e);
			return "Error: could not parse function arguments";
		}

		return functionExecutor.execute(name, arguments, patient);
	}

	private Map<String, Object> toolResultMessage(Map<String, Object> toolCall, String result) {
		return Map.of("role", "tool", "tool_call_id", toolCall.get("id"), "content", result);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractMessage(Map<String, Object> response) {
		List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
		return (Map<String, Object>) choices.get(0).get("message");
	}

	private List<Map<String, Object>> buildMessageHistory(Patient patient, String latestInbound) {
		List<ConversationMessage> recent = conversationMessageRepository
				.findTop20ByPatientIdOrderByCreatedAtDesc(patient.getId());
		Collections.reverse(recent);

		List<Map<String, Object>> messages = new ArrayList<>();
		messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

		for (ConversationMessage m : recent) {
			String role = m.getDirection() == ConversationMessage.Direction.INBOUND ? "user" : "assistant";
			messages.add(Map.of("role", role, "content", m.getContent()));
		}
		return messages;
	}

	private void saveMessage(Patient patient, ConversationMessage.Direction direction, String content,
			String actionTaken) {
		ConversationMessage message = new ConversationMessage();
		message.setPatient(patient);
		message.setDirection(direction);
		message.setContent(content);
		message.setActionTaken(actionTaken);
		conversationMessageRepository.save(message);
	}
}