package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, String> {
	// Recent history for a patient, used to give the AI conversation context
	List<ConversationMessage> findTop20ByPatientIdOrderByCreatedAtDesc(String patientId);
}