package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationRecord, String> {

	// Scheduler job picks up due, unsent notifications
	List<NotificationRecord> findByStatusAndScheduledForBefore(NotificationRecord.NotificationStatus status,
			LocalDateTime cutoff);

	// Admin portal "would-have-sent" QA view
	List<NotificationRecord> findByPatientIdOrderByScheduledForDesc(String patientId);

	// add this method to the existing NotificationRepository interface
	boolean existsByAppointmentIdAndIntent(String appointmentId, NotificationRecord.NotificationIntent intent);
}