package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.FollowUpResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpResponseRepository extends JpaRepository<FollowUpResponse, String> {
	List<FollowUpResponse> findByEscalatedTrueOrderByCreatedAtDesc();
}