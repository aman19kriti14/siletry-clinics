package com.gatistack.siletry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.Doctor;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.AppointmentRepository;
import com.gatistack.siletry.repository.DoctorRepository;

@Service
public class BookingService {

	private final AppointmentRepository appointmentRepository;
	private final DoctorRepository doctorRepository;

	public BookingService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRepository = doctorRepository;
	}

	public Appointment book(Patient patient, String doctorId, LocalDateTime scheduledAt,
			Appointment.AppointmentType type, Appointment.BookingSource source) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

		checkConflict(doctorId, scheduledAt, doctor.getSlotDurationMinutes());

		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setScheduledAt(scheduledAt);
		appointment.setAppointmentType(type);
		appointment.setBookingSource(source);
		return appointmentRepository.save(appointment);
	}

	private void checkConflict(String doctorId, LocalDateTime scheduledAt, int slotDurationMinutes) {
		LocalDateTime windowStart = scheduledAt.minusMinutes(slotDurationMinutes - 1);
		LocalDateTime windowEnd = scheduledAt.plusMinutes(slotDurationMinutes - 1);
		List<Appointment> overlapping = appointmentRepository.findByDoctorIdAndScheduledAtBetween(doctorId, windowStart,
				windowEnd);
		if (!overlapping.isEmpty()) {
			throw new IllegalStateException("Doctor already has an appointment in this slot");
		}
	}

	public List<Appointment> todaysSchedule() {
		LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		return appointmentRepository.findByScheduledAtBetweenOrderByScheduledAtAsc(start, end);
	}

	public Appointment updateStatus(String appointmentId, Appointment.AppointmentStatus status) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));
		appointment.setStatus(status);
		return appointmentRepository.save(appointment);
	}

	// BookingService addition
	public List<java.time.LocalDateTime> availableSlots(String doctorId, java.time.LocalDate date) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

		// v1: fixed 9am-5pm working window - workingHoursJson parsing deferred until
		// per-doctor schedules are actually needed (flagging as a simplification, not
		// an oversight)
		java.time.LocalDateTime dayStart = date.atTime(9, 0);
		java.time.LocalDateTime dayEnd = date.atTime(17, 0);
		int slotMinutes = doctor.getSlotDurationMinutes();

		List<Appointment> booked = appointmentRepository.findByDoctorIdAndScheduledAtBetween(doctorId, dayStart,
				dayEnd);
		java.util.Set<java.time.LocalDateTime> bookedTimes = booked.stream().map(Appointment::getScheduledAt)
				.collect(java.util.stream.Collectors.toSet());

		List<java.time.LocalDateTime> available = new java.util.ArrayList<>();
		java.time.LocalDateTime cursor = dayStart;
		while (cursor.isBefore(dayEnd)) {
			if (!bookedTimes.contains(cursor)) {
				available.add(cursor);
			}
			cursor = cursor.plusMinutes(slotMinutes);
		}
		return available;
	}

	public Appointment reschedule(String appointmentId, LocalDateTime newDateTime) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

		if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED
				|| appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
			throw new IllegalStateException("Cannot reschedule a " + appointment.getStatus() + " appointment");
		}

		checkConflict(appointment.getDoctor().getId(), newDateTime, appointment.getDoctor().getSlotDurationMinutes());

		appointment.setScheduledAt(newDateTime);
		appointment.setStatus(Appointment.AppointmentStatus.BOOKED); // reset in case it was CONFIRMED
		return appointmentRepository.save(appointment);
	}
}