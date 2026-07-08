package com.gatistack.siletry.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gatistack.siletry.entity.Feedback;
import com.gatistack.siletry.service.FeedbackService;
import com.gatistack.siletry.service.PatientService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

	private final FeedbackService feedbackService;
	private final PatientService patientService;

	public FeedbackController(FeedbackService feedbackService, PatientService patientService) {
		this.feedbackService = feedbackService;
		this.patientService = patientService;
	}

	public record SubmitFeedbackRequest(@NotBlank String patientId, String appointmentId,
			@NotNull @Min(1) @Max(5) Integer rating, String comment) {
	}

	@PostMapping
	public Feedback submit(@RequestBody SubmitFeedbackRequest request) {
		var patient = patientService.getById(request.patientId());
		// appointmentId intentionally not resolved to an entity here to keep this
		// endpoint simple - full appointment lookup can be added when the WhatsApp
		// feedback-collection flow is built and actually needs it
		return feedbackService.submit(patient, null, request.rating(), request.comment());
	}

	@GetMapping("/escalated")
	public List<Feedback> escalated() {
		return feedbackService.getEscalated();
	}
}