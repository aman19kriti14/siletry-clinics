package com.gatistack.siletry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "location")
@Getter
@Setter
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(nullable = false)
	private String name; // e.g. "Kochi Branch", "Thrissur Branch"

	private String address;
	private String phone;

	@Column(nullable = false)
	private boolean active = true;
}