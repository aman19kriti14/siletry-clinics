package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "doctor")
@Getter
@Setter
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String name;

	private String specialization;

	@Column(nullable = false)
	private Integer slotDurationMinutes = 15;

	// Structured working hours - kept simple for v1 (JSON string), can normalize
	// into
	// a separate table later if per-day overrides get complex
	@Column(columnDefinition = "TEXT")
	private String workingHoursJson;

	private boolean active = true;
}