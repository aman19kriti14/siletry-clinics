package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class Feedback {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id")
	private Appointment appointment; // nullable - general feedback may not tie to a specific visit

	@Column(nullable = false)
	private Integer rating; // 1-5

	@Column(columnDefinition = "TEXT")
	private String comment;

	@Enumerated(EnumType.STRING)
	private Sentiment sentiment;

	@Column(nullable = false)
	private boolean escalated = false;

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Sentiment {
		POSITIVE, NEUTRAL, NEGATIVE
	}
}