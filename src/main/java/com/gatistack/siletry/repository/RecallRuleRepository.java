package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.RecallRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecallRuleRepository extends JpaRepository<RecallRule, String> {
	List<RecallRule> findByActiveTrue();
}