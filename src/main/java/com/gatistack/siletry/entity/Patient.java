package com.gatistack.siletry.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "patient")
@Getter
@Setter
public class Patient {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String phone; // primary contact, NOT the primary key - patient identity is this record, not
							// the number

	private String email;
	private LocalDate dob;
	private Integer age;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	private ChannelPreference channelPreference = ChannelPreference.UNSET;

	@Enumerated(EnumType.STRING)
	private CreatedVia createdVia;

	@Column(nullable = false)
	private String preferredLanguage = "en"; // ISO code: "en", "hi", "ml", etc.

	private LocalDateTime createdAt = LocalDateTime.now();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ConversationMode conversationMode = ConversationMode.AI;

	public enum ConversationMode {
		AI, STAFF
	}

	public enum Gender {
		MALE, FEMALE, OTHER, UNDISCLOSED
	}

	public enum ChannelPreference {
		WHATSAPP, SMS, VOICE, UNSET
	}

	// whatsapp_inbound reserved for when the channel adapter goes live
	public enum CreatedVia {
		WALK_IN, PHONE, MANUAL_ADMIN, WHATSAPP_INBOUND
	}
}