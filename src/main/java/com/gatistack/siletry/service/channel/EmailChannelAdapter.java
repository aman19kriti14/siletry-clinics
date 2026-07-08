package com.gatistack.siletry.service.channel;

import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelAdapter implements ChannelAdapter {

	private static final Logger log = LoggerFactory.getLogger(EmailChannelAdapter.class);

	private final String apiKey;
	private final String fromEmail;

	public EmailChannelAdapter(@Value("${sendgrid.api.key}") String apiKey,
			@Value("${sendgrid.from.email}") String fromEmail) {
		this.apiKey = apiKey;
		this.fromEmail = fromEmail;
	}

	@Override
	public DeliveryResult send(NotificationRecord notification, Patient patient) {
		if (patient.getEmail() == null || patient.getEmail().isBlank()) {
			return new DeliveryResult(false, "Patient has no email on file");
		}

		Email from = new Email(fromEmail);
		Email to = new Email(patient.getEmail());
		String subject = subjectFor(notification.getIntent());
		Content content = new Content("text/plain", notification.getPayload());
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(apiKey);
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);

			boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
			if (!success) {
				log.warn("SendGrid returned non-success status {} for patient {}: {}", response.getStatusCode(),
						patient.getId(), response.getBody());
			}
			return new DeliveryResult(success, "SendGrid status: " + response.getStatusCode());

		} catch (Exception e) {
			log.error("Failed to send email via SendGrid for patient {}", patient.getId(), e);
			return new DeliveryResult(false, "Email send failed: " + e.getMessage());
		}
	}

	@Override
	public boolean isAvailableFor(Patient patient) {
		return patient.getEmail() != null && !patient.getEmail().isBlank();
	}

	private String subjectFor(NotificationRecord.NotificationIntent intent) {
		return switch (intent) {
		case APPOINTMENT_REMINDER_24H, APPOINTMENT_REMINDER_2H -> "Appointment Reminder - Siletry";
		case POST_VISIT_FOLLOWUP -> "How are you feeling? - Siletry";
		case RECALL -> "Time for a follow-up visit - Siletry";
		};
	}
}