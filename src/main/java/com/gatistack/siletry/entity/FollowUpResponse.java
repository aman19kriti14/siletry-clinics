package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow_up_response")
@Getter
@Setter
public class FollowUpResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "notification_id")
	private NotificationRecord notification; // the POST_VISIT_FOLLOWUP this reply is answering

	@Column(columnDefinition = "TEXT", nullable = false)
	private String replyText;

	@Enumerated(EnumType.STRING)
	private Assessment assessment;

	@Column(columnDefinition = "TEXT")
	private String aiReasoning; // brief explanation, useful for staff review and debugging false escalations

	@Column(nullable = false)
	private boolean escalated = false;

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Assessment {
		OK, CONCERNING, UNCLEAR
	}
}