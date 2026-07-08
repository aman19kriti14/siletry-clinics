package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalationRepository extends JpaRepository<Escalation, String> {
	List<Escalation> findByResolvedFalseOrderByCreatedAtDesc();
}