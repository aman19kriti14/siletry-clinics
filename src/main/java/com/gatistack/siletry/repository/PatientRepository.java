package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {

	// Front-desk lookup during walk-in/phone booking - quick match by phone
	Optional<Patient> findByPhone(String phone);

	// Search by partial name for the admin portal's patient list
	List<Patient> findByNameContainingIgnoreCase(String name);
}