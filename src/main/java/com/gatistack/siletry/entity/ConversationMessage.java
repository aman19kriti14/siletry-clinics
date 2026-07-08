package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_message")
@Getter
@Setter
public class ConversationMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Direction direction;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	// If the AI invoked a function (e.g. book_appointment), record what it did -
	// critical for auditing autonomous actions taken on a real clinic calendar
	@Column(columnDefinition = "TEXT")
	private String actionTaken;

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Direction {
		INBOUND, OUTBOUND
	}
}