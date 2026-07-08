package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.RecallRule;
import com.gatistack.siletry.repository.AppointmentRepository;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.repository.RecallRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecallService {

	private final RecallRuleRepository recallRuleRepository;
	private final AppointmentRepository appointmentRepository;
	private final NotificationRepository notificationRepository;
	private final NotificationService notificationService;

	public RecallService(RecallRuleRepository recallRuleRepository, AppointmentRepository appointmentRepository,
			NotificationRepository notificationRepository, NotificationService notificationService) {
		this.recallRuleRepository = recallRuleRepository;
		this.appointmentRepository = appointmentRepository;
		this.notificationRepository = notificationRepository;
		this.notificationService = notificationService;
	}

	// Called once per tenant per scheduler sweep - see wiring into
	// ReminderScheduler below
	public void processRecalls() {
		List<RecallRule> activeRules = recallRuleRepository.findByActiveTrue();
		LocalDateTime now = LocalDateTime.now();

		for (RecallRule rule : activeRules) {
			LocalDateTime cutoff = now.minusDays(rule.getIntervalDays());

			List<Appointment> candidates = appointmentRepository.findByAppointmentTypeAndStatusAndScheduledAtBefore(
					rule.getAppointmentType(), Appointment.AppointmentStatus.COMPLETED, cutoff);

			for (Appointment appointment : candidates) {
				boolean alreadyCameBack = appointmentRepository.existsByPatientIdAndAppointmentTypeAndScheduledAtAfter(
						appointment.getPatient().getId(), rule.getAppointmentType(), appointment.getScheduledAt());

				if (alreadyCameBack) {
					continue; // patient already booked a follow-up of this type - no recall needed
				}

				boolean alreadyScheduled = notificationRepository.existsByAppointmentIdAndIntent(appointment.getId(),
						NotificationRecord.NotificationIntent.RECALL);

				if (!alreadyScheduled) {
					notificationService.scheduleRecall(appointment, rule);
				}
			}
		}
	}
}