package com.gatistack.siletry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gatistack.siletry.entity.StaffUser;

public interface StaffUserRepository extends JpaRepository<StaffUser, String> {
	Optional<StaffUser> findByEmail(String email);

	List<StaffUser> findByRoleAndPhoneIsNotNull(StaffUser.Role role);
}