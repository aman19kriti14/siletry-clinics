package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.QueueEntry;
import com.gatistack.siletry.repository.AppointmentRepository;
import com.gatistack.siletry.service.QueueService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

	private final QueueService queueService;
	private final AppointmentRepository appointmentRepository;

	public QueueController(QueueService queueService, AppointmentRepository appointmentRepository) {
		this.queueService = queueService;
		this.appointmentRepository = appointmentRepository;
	}

	public record CheckInRequest(@NotBlank String appointmentId) {
	}

	@PostMapping("/check-in")
	public QueueEntry checkIn(@RequestBody CheckInRequest request) {
		Appointment appointment = appointmentRepository.findById(request.appointmentId())
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + request.appointmentId()));
		return queueService.checkIn(appointment);
	}

	@GetMapping("/doctor/{doctorId}/today")
	public List<QueueEntry> todaysQueue(@PathVariable String doctorId) {
		return queueService.todaysQueue(doctorId);
	}

	public record StatusUpdateRequest(@NotBlank String status) {
	}

	@PatchMapping("/{queueEntryId}/status")
	public QueueEntry updateStatus(@PathVariable String queueEntryId, @RequestBody StatusUpdateRequest request) {
		return queueService.updateStatus(queueEntryId, QueueEntry.QueueStatus.valueOf(request.status()));
	}
}