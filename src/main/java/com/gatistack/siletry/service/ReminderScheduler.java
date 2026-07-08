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
			NotificationService notificationService) {
		this.tenantRepository = tenantRepository;
		this.appointmentRepository = appointmentRepository;
		this.notificationService = notificationService;
		this.notificationRepository = null;
		this.recallService = null;
	}

	// Runs every 15 min: sweeps all active tenants, schedules reminders that just
	// entered the 24h/2h window, and dispatches anything already due.
	@Scheduled(fixedRate = 15 * 60 * 1000)
	public void run() {
		// This query itself must hit the master schema, not a tenant schema -
		// TenantContext is cleared here, so TenantIdentifierResolver falls back to
		// "master" (see #2).
		List<Tenant> activeTenants = tenantRepository.findAll().stream()
				.filter(t -> t.getStatus() == Tenant.TenantStatus.ACTIVE || t.getStatus() == Tenant.TenantStatus.TRIAL)
				.toList();

		for (Tenant tenant : activeTenants) {
			try {
				TenantContext.setSchema(tenant.getSchemaName());
				processTenant();
			} catch (Exception e) {
				// One clinic's failure must never block the others in the sweep
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

		// 24h window: appointments between 23h45m and 24h from now that haven't been
		// scheduled yet
		scheduleForWindow(now.plusHours(24).minusMinutes(15), now.plusHours(24),
				NotificationRecord.NotificationIntent.APPOINTMENT_REMINDER_24H);

		// 2h window
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