package com.gatistack.siletry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gatistack.siletry.config.TenantContext;
import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Tenant;
import com.gatistack.siletry.repository.AppointmentRepository;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.repository.TenantRepository;

@Component
public class ReminderScheduler {

	private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

	private final TenantRepository tenantRepository;
	private final AppointmentRepository appointmentRepository;
	private final NotificationService notificationService;
	private final NotificationRepository notificationRepository;
	private final RecallService recallService;

	public ReminderScheduler(TenantRepository tenantRepository, AppointmentRepository appointmentRepository,
			NotificationService notificationService, NotificationRepository notificationRepository,
			RecallService recallService) {
		this.tenantRepository = tenantRepository;
		this.appointmentRepository = appointmentRepository;
		this.notificationService = notificationService;
		this.notificationRepository = notificationRepository;
		this.recallService = recallService;
	}

	@Scheduled(fixedRate = 15 * 60 * 1000)
	public void run() {
		List<Tenant> activeTenants = tenantRepository.findAll().stream()
				.filter(t -> t.getStatus() == Tenant.TenantStatus.ACTIVE || t.getStatus() == Tenant.TenantStatus.TRIAL)
				.toList();

		for (Tenant tenant : activeTenants) {
			try {
				TenantContext.setSchema(tenant.getSchemaName());
				processTenant();
			} catch (Exception e) {
				log.error("Reminder sweep failed for tenant {}: {}", tenant.getSchemaName(), e.getMessage(), e);
			} finally {
				TenantContext.clear();
			}
		}
	}

	private void processTenant() {
		scheduleUpcomingReminders();
		notificationService.dispatchDueNotifications();
		recallService.processRecalls();
	}

	private void scheduleUpcomingReminders() {
		LocalDateTime now = LocalDateTime.now();

		scheduleForWindow(now.plusHours(24).minusMinutes(15), now.plusHours(24),
				NotificationRecord.NotificationIntent.APPOINTMENT_REMINDER_24H);

		scheduleForWindow(now.plusHours(2).minusMinutes(15), now.plusHours(2),
				NotificationRecord.NotificationIntent.APPOINTMENT_REMINDER_2H);
	}

	private void scheduleForWindow(LocalDateTime start, LocalDateTime end,
			NotificationRecord.NotificationIntent intent) {
		List<Appointment> upcoming = appointmentRepository
				.findByStatusAndScheduledAtBetween(Appointment.AppointmentStatus.BOOKED, start, end);

		for (Appointment appointment : upcoming) {
			boolean alreadyScheduled = notificationRepository.existsByAppointmentIdAndIntent(appointment.getId(),
					intent);
			if (!alreadyScheduled) {
				notificationService.scheduleReminder(appointment, intent, LocalDateTime.now());
			}
		}
	}
}