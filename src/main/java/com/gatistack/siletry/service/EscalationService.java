package com.gatistack.siletry.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.Escalation;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.entity.StaffUser;
import com.gatistack.siletry.repository.EscalationRepository;
import com.gatistack.siletry.repository.PatientRepository;
import com.gatistack.siletry.repository.StaffUserRepository;

@Service
public class EscalationService {

	private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

	private final EscalationRepository escalationRepository;
	private final PatientRepository patientRepository;
	private final StaffUserRepository staffUserRepository;
	private final WhatsAppMessageSender whatsAppMessageSender;

	public EscalationService(EscalationRepository escalationRepository, PatientRepository patientRepository,
			WhatsAppMessageSender whatsAppMessageSender, StaffUserRepository staffUserRepository) {
		this.escalationRepository = escalationRepository;
		this.patientRepository = patientRepository;
		this.staffUserRepository = staffUserRepository;
		this.whatsAppMessageSender = whatsAppMessageSender;
	}

	// Stops the AI from replying to this patient and creates a visible alert for
	// staff.
	public Escalation escalate(Patient patient, Escalation.Reason reason, String detail) {
		patient.setConversationMode(Patient.ConversationMode.STAFF);
		patientRepository.save(patient);

		Escalation escalation = new Escalation();
		escalation.setPatient(patient);
		escalation.setReason(reason);
		escalation.setDetail(detail);
		Escalation saved = escalationRepository.save(escalation);

		notifyStaff(patient, reason, detail);

		// Real alert mechanism is still just this log line + the dashboard list below -
		// no push notification/SMS-to-staff exists yet. Flagging this as a real gap,
		// not solved by this change alone - see note after the code.
		log.warn("ESCALATION [{}] for patient {}: {}", reason, patient.getId(), detail);

		return saved;
	}

	private void notifyStaff(Patient patient, Escalation.Reason reason, String detail) {
		List<StaffUser> owners = staffUserRepository.findByRoleAndPhoneIsNotNull(StaffUser.Role.OWNER);
		String alertText = "Alert: patient " + patient.getName() + " (" + patient.getPhone() + ") needs attention - "
				+ reason + ". \"" + detail + "\"";

		for (StaffUser owner : owners) {
			try {
				// NOTE: this is a business-initiated message to staff - same 24h/template
				// rule as patient reminders applies. Will fail until templates are approved,
				// same as everything else waiting on that.
				whatsAppMessageSender.sendText(owner.getPhone(), alertText);
			} catch (Exception e) {
				log.error("Failed to send WhatsApp escalation alert to staff {}", owner.getId(), e);
			}
		}
	}

	public List<Escalation> getActive() {
		return escalationRepository.findByResolvedFalseOrderByCreatedAtDesc();
	}

	// Staff takes back manual control and marks the escalation resolved
	public void resolveAndReturnToAi(String escalationId) {
		Escalation escalation = escalationRepository.findById(escalationId)
				.orElseThrow(() -> new IllegalArgumentException("Escalation not found: " + escalationId));
		escalation.setResolved(true);
		escalation.setResolvedAt(LocalDateTime.now());
		escalationRepository.save(escalation);

		Patient patient = escalation.getPatient();
		patient.setConversationMode(Patient.ConversationMode.AI);
		patientRepository.save(patient);
	}
}