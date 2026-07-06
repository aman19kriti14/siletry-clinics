package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	public record QuickAddRequest(@NotBlank String name, @NotBlank String phone, String createdVia) {
	}

	@PostMapping("/quick-add")
	public Patient quickAdd(@Valid @RequestBody QuickAddRequest request) {
		Patient.CreatedVia via = request.createdVia() != null ? Patient.CreatedVia.valueOf(request.createdVia())
				: Patient.CreatedVia.MANUAL_ADMIN;
		return patientService.findOrCreate(request.name(), request.phone(), via);
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