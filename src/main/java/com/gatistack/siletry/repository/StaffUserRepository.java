package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, String> {
	Optional<StaffUser> findByEmail(String email);
}