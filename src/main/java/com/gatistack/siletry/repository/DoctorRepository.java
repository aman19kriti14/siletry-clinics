package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, String> {
    List<Doctor> findByActiveTrue();
}