package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Doctor;
import com.gatistack.siletry.repository.DoctorRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

	private final DoctorRepository doctorRepository;

	public DoctorController(DoctorRepository doctorRepository) {
		this.doctorRepository = doctorRepository;
	}

	@GetMapping
	public List<Doctor> listActive() {
		return doctorRepository.findByActiveTrue();
	}

	@PostMapping
	public Doctor create(@Valid @RequestBody Doctor doctor) {
		return doctorRepository.save(doctor);
	}
}