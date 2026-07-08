package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recall_rule")
@Getter
@Setter
public class RecallRule {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Appointment.AppointmentType appointmentType;

	@Column(nullable = false)
	private Integer intervalDays; // e.g. 180 for a 6-month checkup recall

	@Column(nullable = false)
	private boolean active = true;

	@Column(columnDefinition = "TEXT")
	private String messageTemplate; // e.g. "It's been a while since your last checkup - time to book another?"
}