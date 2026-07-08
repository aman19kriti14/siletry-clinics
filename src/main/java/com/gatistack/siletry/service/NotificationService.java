package com.gatistack.siletry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.entity.RecallRule;
import com.gatistack.siletry.repository.NotificationRepository;
import com.gatistack.siletry.service.channel.ChannelAdapter;
import com.gatistack.siletry.service.channel.StubChannelAdapter;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final List<ChannelAdapter> channelAdapters; // Spring injects every ChannelAdapter bean here
	private final StubChannelAdapter fallbackAdapter;

	public NotificationService(NotificationRepository notificationRepository, List<ChannelAdapter> channelAdapters,
			StubChannelAdapter fallbackAdapter) {
		this.notificationRepository = notificationRepository;
		this.channelAdapters = channelAdapters;
		this.fallbackAdapter = fallbackAdapter;
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
			ChannelAdapter adapter = selectAdapter(patient);
			ChannelAdapter.DeliveryResult result = adapter.send(notification, patient);

			notification.setStatus(result.success() ? NotificationRecord.NotificationStatus.SENT
					: NotificationRecord.NotificationStatus.SKIPPED_NO_CHANNEL);
			notification.setChannelUsed(patient.getChannelPreference());
			notification.setSentAt(LocalDateTime.now());
			notificationRepository.save(notification);
		}
	}

	// Picks the first real adapter that's actually usable for this patient
	// (e.g. EmailChannelAdapter if they have an email on file), falling back
	// to the stub "would-have-sent" logger if nothing else applies.
	private ChannelAdapter selectAdapter(Patient patient) {
		return channelAdapters.stream().filter(adapter -> adapter != fallbackAdapter && adapter.isAvailableFor(patient))
				.findFirst().orElse(fallbackAdapter);
	}

	private String buildPayload(NotificationRecord.NotificationIntent intent, Appointment appointment) {
		return switch (intent) {
		case APPOINTMENT_REMINDER_24H -> "Reminder: your appointment with Dr. " + appointment.getDoctor().getName()
				+ " is tomorrow at " + appointment.getScheduledAt();
		case APPOINTMENT_REMINDER_2H ->
			"Reminder: your appointment with Dr. " + appointment.getDoctor().getName() + " is in 2 hours.";
		case POST_VISIT_FOLLOWUP -> "How are you feeling after your visit? Reply to let us know.";
		case RECALL -> "It's been a while since your last visit - time to book a follow-up?";
		};
	}

	// New method - add alongside the existing scheduleReminder
	public NotificationRecord scheduleRecall(Appointment appointment, RecallRule rule) {
		NotificationRecord notification = new NotificationRecord();
		notification.setPatient(appointment.getPatient());
		notification.setAppointment(appointment);
		notification.setIntent(NotificationRecord.NotificationIntent.RECALL);
		notification.setScheduledFor(LocalDateTime.now());
		notification.setPayload(rule.getMessageTemplate() != null ? rule.getMessageTemplate()
				: "It's been a while since your last visit - time to book a follow-up?");
		return notificationRepository.save(notification);
	}
}