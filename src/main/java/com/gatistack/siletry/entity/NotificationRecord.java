package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class NotificationRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id")
	private Appointment appointment; // nullable - recall/general notifications won't have one

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationIntent intent;

	@Column(nullable = false)
	private LocalDateTime scheduledFor;

	@Enumerated(EnumType.STRING)
	private NotificationStatus status = NotificationStatus.PENDING;

	@Enumerated(EnumType.STRING)
	private Patient.ChannelPreference channelUsed; // null until an adapter actually dispatches it

	@Column(columnDefinition = "TEXT")
	private String payload; // templated content, channel-independent

	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime sentAt;

	public enum NotificationIntent {
		APPOINTMENT_REMINDER_24H, APPOINTMENT_REMINDER_2H, POST_VISIT_FOLLOWUP, RECALL
	}

	public enum NotificationStatus {
		PENDING, SENT, FAILED, SKIPPED_NO_CHANNEL
	}
}