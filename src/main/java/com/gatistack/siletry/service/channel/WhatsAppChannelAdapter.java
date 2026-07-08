package com.gatistack.siletry.service.channel;

import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.service.WhatsAppMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhatsAppChannelAdapter implements ChannelAdapter {

	private static final Logger log = LoggerFactory.getLogger(WhatsAppChannelAdapter.class);

	private final WhatsAppMessageSender sender;

	public WhatsAppChannelAdapter(WhatsAppMessageSender sender) {
		this.sender = sender;
	}

	@Override
	public DeliveryResult send(NotificationRecord notification, Patient patient) {
		// Template name mapping - THESE MUST EXIST AND BE APPROVED in Meta's WhatsApp
		// Manager before this will work. Placeholder names below - replace with your
		// actual approved template names once created.
		String templateName = switch (notification.getIntent()) {
		case APPOINTMENT_REMINDER_24H -> "appointment_reminder_24h";
		case APPOINTMENT_REMINDER_2H -> "appointment_reminder_2h";
		case POST_VISIT_FOLLOWUP -> "post_visit_followup";
		case RECALL -> "recall_checkup";
		};

		try {
			sender.sendTemplate(patient.getPhone(), templateName, "en", List.of(patient.getName()));
			return new DeliveryResult(true, "Sent via WhatsApp template: " + templateName);
		} catch (Exception e) {
			log.error("Failed to send WhatsApp template message to patient {}", patient.getId(), e);
			return new DeliveryResult(false, "WhatsApp send failed: " + e.getMessage());
		}
	}

	@Override
	public boolean isAvailableFor(Patient patient) {
		return patient.getChannelPreference() == Patient.ChannelPreference.WHATSAPP && patient.getPhone() != null
				&& !patient.getPhone().isBlank();
	}
}