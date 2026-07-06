package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Getter
@Setter
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@Column(nullable = false)
	private LocalDateTime scheduledAt;

	@Enumerated(EnumType.STRING)
	private AppointmentType appointmentType;

	@Enumerated(EnumType.STRING)
	private AppointmentStatus status = AppointmentStatus.BOOKED;

	@Enumerated(EnumType.STRING)
	private BookingSource bookingSource;

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum AppointmentType {
		CONSULTATION, FOLLOW_UP, CHECKUP
	}

	public enum AppointmentStatus {
		BOOKED, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED
	}

	// whatsapp reserved for when that channel goes live
	public enum BookingSource {
		WALK_IN, PHONE_MANUAL, ADMIN, WHATSAPP
	}
}