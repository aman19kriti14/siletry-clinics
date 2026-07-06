package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

	// Today's schedule view (admin portal home screen)
	List<Appointment> findByScheduledAtBetweenOrderByScheduledAtAsc(LocalDateTime start, LocalDateTime end);

	List<Appointment> findByDoctorIdAndScheduledAtBetween(String doctorId, LocalDateTime start, LocalDateTime end);

	List<Appointment> findByPatientIdOrderByScheduledAtDesc(String patientId);

	// Used by the reminder scheduler to find appointments needing a 24h/2h
	// notification
	List<Appointment> findByStatusAndScheduledAtBetween(Appointment.AppointmentStatus status, LocalDateTime start,
			LocalDateTime end);
}