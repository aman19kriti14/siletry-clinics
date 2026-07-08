package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, String> {

	List<QueueEntry> findByAppointment_Doctor_IdAndCheckedInAtBetweenOrderByTokenNumberAsc(String doctorId,
			LocalDateTime start, LocalDateTime end);

	Optional<QueueEntry> findByAppointmentId(String appointmentId);

	// Used to compute the next token number for a doctor's queue today
	int countByAppointment_Doctor_IdAndCheckedInAtBetween(String doctorId, LocalDateTime start, LocalDateTime end);
}