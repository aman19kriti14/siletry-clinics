package com.gatistack.siletry.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.service.PatientService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	public record QuickAddRequest(@NotBlank String name,
			@NotBlank @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "Phone must be in E.164 format, e.g. +919876543210") String phone,
			String createdVia, String preferredLanguage) {
	}

	@PostMapping("/quick-add")
	public Patient quickAdd(@Valid @RequestBody QuickAddRequest request) {
		Patient.CreatedVia via = request.createdVia() != null ? Patient.CreatedVia.valueOf(request.createdVia())
				: Patient.CreatedVia.MANUAL_ADMIN;
		return patientService.findOrCreate(request.name(), request.phone(), via, request.preferredLanguage());
	}

	@GetMapping("/{id}")
	public Patient getById(@PathVariable String id) {
		return patientService.getById(id);
	}

	@GetMapping("/search")
	public List<Patient> search(@RequestParam String q) {
		return patientService.search(q);
	}

	@PutMapping("/{id}")
	public Patient update(@PathVariable String id, @RequestBody Patient updates) {
		return patientService.update(id, updates);
	}
}