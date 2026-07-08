package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.RecallRule;
import com.gatistack.siletry.repository.RecallRuleRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recall-rules")
public class RecallRuleController {

	private final RecallRuleRepository recallRuleRepository;

	public RecallRuleController(RecallRuleRepository recallRuleRepository) {
		this.recallRuleRepository = recallRuleRepository;
	}

	public record CreateRuleRequest(@NotBlank String appointmentType, @NotNull @Positive Integer intervalDays,
			String messageTemplate) {
	}

	@PostMapping
	public RecallRule create(@RequestBody CreateRuleRequest request) {
		RecallRule rule = new RecallRule();
		rule.setAppointmentType(Appointment.AppointmentType.valueOf(request.appointmentType()));
		rule.setIntervalDays(request.intervalDays());
		rule.setMessageTemplate(request.messageTemplate());
		return recallRuleRepository.save(rule);
	}

	@GetMapping
	public List<RecallRule> listActive() {
		return recallRuleRepository.findByActiveTrue();
	}

	public record UpdateRuleRequest(Integer intervalDays, String messageTemplate, Boolean active) {
	}

	@PatchMapping("/{id}")
	public RecallRule update(@PathVariable String id, @RequestBody UpdateRuleRequest request) {
		RecallRule rule = recallRuleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Recall rule not found: " + id));

		if (request.intervalDays() != null)
			rule.setIntervalDays(request.intervalDays());
		if (request.messageTemplate() != null)
			rule.setMessageTemplate(request.messageTemplate());
		if (request.active() != null)
			rule.setActive(request.active());

		return recallRuleRepository.save(rule);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable String id) {
		recallRuleRepository.deleteById(id);
	}
}