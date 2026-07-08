package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalation")
@Getter
@Setter
public class Escalation {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Reason reason;

	@Column(columnDefinition = "TEXT")
	private String detail;

	@Column(nullable = false)
	private boolean resolved = false;

	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime resolvedAt;

	public enum Reason {
		EMERGENCY_KEYWORD, AI_UNCERTAIN, CONCERNING_FOLLOWUP, PATIENT_REQUESTED_HUMAN
	}
}