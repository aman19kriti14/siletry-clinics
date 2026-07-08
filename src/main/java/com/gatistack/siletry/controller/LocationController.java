package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Location;
import com.gatistack.siletry.repository.LocationRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

	private final LocationRepository locationRepository;

	public LocationController(LocationRepository locationRepository) {
		this.locationRepository = locationRepository;
	}

	@GetMapping
	public List<Location> listActive() {
		return locationRepository.findByActiveTrue();
	}

	@PostMapping
	public Location create(@Valid @RequestBody Location location) {
		return locationRepository.save(location);
	}
}