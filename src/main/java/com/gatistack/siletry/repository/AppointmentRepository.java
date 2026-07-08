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

	// Candidates for recall: completed appointments of a given type, old enough per
	// the rule's interval
	List<Appointment> findByAppointmentTypeAndStatusAndScheduledAtBefore(Appointment.AppointmentType type,
			Appointment.AppointmentStatus status, LocalDateTime cutoff);

	// Has the patient already come back for this appointment type since?
	boolean existsByPatientIdAndAppointmentTypeAndScheduledAtAfter(String patientId, Appointment.AppointmentType type,
			LocalDateTime after);

	// Aggregate counts for a date range, used by the analytics dashboard
	long countByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

	long countByStatusAndScheduledAtBetween(Appointment.AppointmentStatus status, LocalDateTime start,
			LocalDateTime end);
}