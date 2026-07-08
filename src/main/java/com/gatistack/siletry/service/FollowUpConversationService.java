package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.FollowUpResponse;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.FollowUpResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowUpConversationService {

	private static final Logger log = LoggerFactory.getLogger(FollowUpConversationService.class);

	private static final String SYSTEM_PROMPT = """
			You are a clinical triage assistant reviewing a patient's reply to a post-visit
			check-in message from their clinic. Classify the reply as one of exactly:
			OK, CONCERNING, or UNCLEAR.

			- OK: patient reports feeling fine or improving, no red flags.
			- CONCERNING: patient reports worsening symptoms, new severe symptoms, pain,
			  inability to function, or anything that should prompt a staff member to
			  follow up directly rather than waiting.
			- UNCLEAR: the reply doesn't give enough information to judge either way.

			Respond in exactly this format, nothing else:
			ASSESSMENT: <OK|CONCERNING|UNCLEAR>
			REASONING: <one brief sentence>
			""";

	private final OpenAiService openAiService;
	private final FollowUpResponseRepository followUpResponseRepository;

	public FollowUpConversationService(OpenAiService openAiService,
			FollowUpResponseRepository followUpResponseRepository) {
		this.openAiService = openAiService;
		this.followUpResponseRepository = followUpResponseRepository;
	}

	public FollowUpResponse processReply(Patient patient, NotificationRecord notification, String replyText) {
		FollowUpResponse.Assessment assessment;
		String reasoning;

		try {
			String aiOutput = openAiService.chat(SYSTEM_PROMPT, replyText);
			String[] lines = aiOutput.strip().split("\n");
			assessment = FollowUpResponse.Assessment.valueOf(lines[0].replace("ASSESSMENT:", "").strip().toUpperCase());
			reasoning = lines.length > 1 ? lines[1].replace("REASONING:", "").strip() : "";
		} catch (Exception e) {
			// If the AI call fails or returns something unparseable, fail safe toward
			// human review rather than silently dropping the reply - a missed concerning
			// reply is a much worse outcome than an unnecessary staff notification.
			log.error("Failed to get AI assessment for follow-up reply, defaulting to UNCLEAR", e);
			assessment = FollowUpResponse.Assessment.UNCLEAR;
			reasoning = "AI assessment failed - flagged for manual review";
		}

		FollowUpResponse response = new FollowUpResponse();
		response.setPatient(patient);
		response.setNotification(notification);
		response.setReplyText(replyText);
		response.setAssessment(assessment);
		response.setAiReasoning(reasoning);
		response.setEscalated(assessment != FollowUpResponse.Assessment.OK);

		return followUpResponseRepository.save(response);
	}

	public List<FollowUpResponse> getEscalated() {
		return followUpResponseRepository.findByEscalatedTrueOrderByCreatedAtDesc();
	}
}