package com.gatistack.siletry.controller;

import com.gatistack.siletry.entity.Escalation;
import com.gatistack.siletry.service.EscalationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escalations")
public class EscalationController {

	private final EscalationService escalationService;

	public EscalationController(EscalationService escalationService) {
		this.escalationService = escalationService;
	}

	@GetMapping
	public List<Escalation> active() {
		return escalationService.getActive();
	}

	@PostMapping("/{id}/resolve")
	public void resolve(@PathVariable String id) {
		escalationService.resolveAndReturnToAi(id);
	}
}