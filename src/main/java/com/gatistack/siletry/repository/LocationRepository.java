package com.gatistack.siletry.repository;

import com.gatistack.siletry.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, String> {
	List<Location> findByActiveTrue();
}