package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

	private final PatientRepository patientRepository;

	public PatientService(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	// Quick-add flow for walk-ins/phone bookings - matches existing patient by
	// phone first
	public Patient findOrCreate(String name, String phone, Patient.CreatedVia createdVia, String preferredLanguage) {
		Optional<Patient> existing = patientRepository.findByPhone(phone);
		if (existing.isPresent()) {
			return existing.get();
		}
		Patient patient = new Patient();
		patient.setName(name);
		patient.setPhone(phone);
		patient.setCreatedVia(createdVia);
		patient.setPreferredLanguage(preferredLanguage != null ? preferredLanguage : "en");
		return patientRepository.save(patient);
	}

	public Patient getById(String id) {
		return patientRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
	}

	public List<Patient> search(String nameQuery) {
		return patientRepository.findByNameContainingIgnoreCase(nameQuery);
	}

	public Patient update(String id, Patient updates) {
		Patient existing = getById(id);
		existing.setName(updates.getName());
		existing.setEmail(updates.getEmail());
		existing.setDob(updates.getDob());
		existing.setGender(updates.getGender());
		existing.setChannelPreference(updates.getChannelPreference());
		return patientRepository.save(existing);
	}
}