package com.gatistack.siletry.entity;

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
@Table(name = "tenant", schema = "master")
@Getter
@Setter
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, unique = true)
	private String schemaName; // e.g. "clinic_abc123" - drives schema routing

	@Column(nullable = false)
	private String clinicName;

	private String address;
	private String phone;
	private String email;

	@Enumerated(EnumType.STRING)
	private SubscriptionTier subscriptionTier = SubscriptionTier.PHASE_1;

	@Enumerated(EnumType.STRING)
	private TenantStatus status = TenantStatus.TRIAL;

	private LocalDateTime trialStartDate; // set on activation, not signup - see architecture doc
	private LocalDateTime trialEndDate;

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum SubscriptionTier {
		PHASE_1, PHASE_2
	}

	public enum TenantStatus {
		TRIAL, ACTIVE, SUSPENDED, CHURNED
	}
}