package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationRepository notificationRepository;

	public NotificationController(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	@GetMapping("/patient/{patientId}")
	public List<NotificationRecord> forPatient(@PathVariable String patientId) {
		return notificationRepository.findByPatientIdOrderByScheduledForDesc(patientId);
	}
}