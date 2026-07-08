package com.gatistack.siletry.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gatistack.siletry.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, String> {
	List<Feedback> findByEscalatedTrueOrderByCreatedAtDesc();

	List<Feedback> findByPatientIdOrderByCreatedAtDesc(String patientId);

	List<Feedback> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}