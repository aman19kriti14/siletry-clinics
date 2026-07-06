package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.service.channel.ChannelAdapter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ChannelAdapter channelAdapter; // v1: StubChannelAdapter injected here;
													// swap for WhatsAppChannelAdapter later, no other changes needed

	public NotificationService(NotificationRepository notificationRepository, ChannelAdapter channelAdapter) {
		this.notificationRepository = notificationRepository;
		this.channelAdapter = channelAdapter;
	}

	// Called by the scheduler (BookingService/scheduler wiring in a moment) when an
	// appointment enters the 24h/2h reminder window
	public NotificationRecord scheduleReminder(Appointment appointment, NotificationRecord.NotificationIntent intent,
			LocalDateTime scheduledFor) {
		NotificationRecord notification = new NotificationRecord();
		notification.setPatient(appointment.getPatient());
		notification.setAppointment(appointment);
		notification.setIntent(intent);
		notification.setScheduledFor(scheduledFor);
		notification.setPayload(buildPayload(intent, appointment));
		return notificationRepository.save(notification);
	}

	// Picked up by a scheduled job per tenant (see note in #4 about looping tenant
	// context)
	public void dispatchDueNotifications() {
		List<NotificationRecord> due = notificationRepository
				.findByStatusAndScheduledForBefore(NotificationRecord.NotificationStatus.PENDING, LocalDateTime.now());

		for (NotificationRecord notification : due) {
			Patient patient = notification.getPatient();
			ChannelAdapter.DeliveryResult result = channelAdapter.send(notification, patient);

			notification.setStatus(result.success() ? NotificationRecord.NotificationStatus.SENT
					: NotificationRecord.NotificationStatus.SKIPPED_NO_CHANNEL);
			notification.setChannelUsed(patient.getChannelPreference());
			notification.setSentAt(LocalDateTime.now());
			notificationRepository.save(notification);
		}
	}

	private String buildPayload(NotificationRecord.NotificationIntent intent, Appointment appointment) {
		return switch (intent) {
		case APPOINTMENT_REMINDER_24H -> "Reminder: your appointment with Dr. " + appointment.getDoctor().getName()
				+ " is tomorrow at " + appointment.getScheduledAt();
		case APPOINTMENT_REMINDER_2H ->
			"Reminder: your appointment with Dr. " + appointment.getDoctor().getName() + " is in 2 hours.";
		case POST_VISIT_FOLLOWUP -> "How are you feeling after your visit? Reply to let us know.";
		};
	}
}