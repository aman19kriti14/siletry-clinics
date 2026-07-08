package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.FollowUpResponse;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.service.FollowUpConversationService;
import com.gatistack.siletry.service.PatientService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow-up")
public class FollowUpController {

	private final FollowUpConversationService followUpConversationService;
	private final PatientService patientService;
	private final NotificationRepository notificationRepository;

	public FollowUpController(FollowUpConversationService followUpConversationService, PatientService patientService,
			NotificationRepository notificationRepository) {
		this.followUpConversationService = followUpConversationService;
		this.patientService = patientService;
		this.notificationRepository = notificationRepository;
	}

	public record ReplyRequest(@NotBlank String patientId, String notificationId, @NotBlank String replyText) {
	}

	@PostMapping("/reply")
	public FollowUpResponse reply(@RequestBody ReplyRequest request) {
		var patient = patientService.getById(request.patientId());
		NotificationRecord notification = request.notificationId() != null
				? notificationRepository.findById(request.notificationId()).orElse(null)
				: null;
		return followUpConversationService.processReply(patient, notification, request.replyText());
	}

	@GetMapping("/escalated")
	public List<FollowUpResponse> escalated() {
		return followUpConversationService.getEscalated();
	}
}