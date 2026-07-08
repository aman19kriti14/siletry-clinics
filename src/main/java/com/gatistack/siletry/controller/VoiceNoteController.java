package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.FollowUpResponse;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.service.FollowUpConversationService;
import com.gatistack.siletry.service.PatientService;
import com.gatistack.siletry.service.SarvamSttService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/voice-notes")
public class VoiceNoteController {

	private final SarvamSttService sarvamSttService;
	private final FollowUpConversationService followUpConversationService;
	private final PatientService patientService;
	private final NotificationRepository notificationRepository;

	public VoiceNoteController(SarvamSttService sarvamSttService,
			FollowUpConversationService followUpConversationService, PatientService patientService,
			NotificationRepository notificationRepository) {
		this.sarvamSttService = sarvamSttService;
		this.followUpConversationService = followUpConversationService;
		this.patientService = patientService;
		this.notificationRepository = notificationRepository;
	}

	@PostMapping(value = "/transcribe-reply", consumes = "multipart/form-data")
	public FollowUpResponse transcribeAndProcess(@RequestParam String patientId,
			@RequestParam(required = false) String notificationId, @RequestParam("audio") MultipartFile audioFile)
			throws IOException {

		String transcript = sarvamSttService.transcribe(audioFile.getBytes(), audioFile.getOriginalFilename());

		var patient = patientService.getById(patientId);
		NotificationRecord notification = notificationId != null
				? notificationRepository.findById(notificationId).orElse(null)
				: null;

		return followUpConversationService.processReply(patient, notification, transcript);
	}
}