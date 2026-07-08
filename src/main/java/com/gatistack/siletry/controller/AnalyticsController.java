package com.gatistack.siletry.controller;

import com.gatistack.siletry.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/summary")
	public AnalyticsService.DashboardSummary summary(@RequestParam String startDate, // yyyy-MM-dd
			@RequestParam String endDate) {

		LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
		LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(); // inclusive of endDate

		return analyticsService.getSummary(start, end);
	}
}