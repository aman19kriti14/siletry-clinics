package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.service.BookingService;
import com.gatistack.siletry.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

	private final BookingService bookingService;
	private final PatientService patientService;

	public AppointmentController(BookingService bookingService, PatientService patientService) {
		this.bookingService = bookingService;
		this.patientService = patientService;
	}

	public record BookRequest(@NotBlank String patientId, @NotBlank String doctorId, @NotNull LocalDateTime scheduledAt,
			@NotBlank String appointmentType, String bookingSource) {
	}

	@PostMapping
	public Appointment book(@Valid @RequestBody BookRequest request) {
		Patient patient = patientService.getById(request.patientId());
		Appointment.BookingSource source = request.bookingSource() != null
				? Appointment.BookingSource.valueOf(request.bookingSource())
				: Appointment.BookingSource.ADMIN;
		return bookingService.book(patient, request.doctorId(), request.scheduledAt(),
				Appointment.AppointmentType.valueOf(request.appointmentType()), source);
	}

	@GetMapping("/today")
	public List<Appointment> today() {
		return bookingService.todaysSchedule();
	}

	public record StatusUpdateRequest(@NotBlank String status) {
	}

	@PatchMapping("/{id}/status")
	public Appointment updateStatus(@PathVariable String id, @Valid @RequestBody StatusUpdateRequest request) {
		return bookingService.updateStatus(id, Appointment.AppointmentStatus.valueOf(request.status()));
	}

	// AppointmentController addition
	@GetMapping("/available-slots")
	public List<LocalDateTime> availableSlots(@RequestParam String doctorId, @RequestParam String date) {
		return bookingService.availableSlots(doctorId, java.time.LocalDate.parse(date));
	}
}