package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_user")
@Getter
@Setter
public class StaffUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String name;

	private String phone;

	@Enumerated(EnumType.STRING)
	private Role role = Role.STAFF;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id")
	private Location location; // null = access to all locations (typically OWNER)

	private LocalDateTime createdAt = LocalDateTime.now();

	public enum Role {
		OWNER, STAFF
	}
}