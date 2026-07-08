package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_entry")
@Getter
@Setter
public class QueueEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private Appointment appointment;

	@Column(nullable = false)
	private Integer tokenNumber; // sequential per doctor per day

	@Enumerated(EnumType.STRING)
	private QueueStatus status = QueueStatus.WAITING;

	private LocalDateTime checkedInAt = LocalDateTime.now();
	private LocalDateTime calledAt;
	private LocalDateTime completedAt;

	public enum QueueStatus {
		WAITING, CALLED, IN_PROGRESS, COMPLETED, NO_SHOW
	}
}