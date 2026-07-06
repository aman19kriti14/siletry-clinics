package com.gatistack.siletry.service.channel;

import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StubChannelAdapter implements ChannelAdapter {

	private static final Logger log = LoggerFactory.getLogger(StubChannelAdapter.class);

	@Override
	public DeliveryResult send(NotificationRecord notification, Patient patient) {
		log.info("[STUB CHANNEL] Would send '{}' to {} ({}): {}", notification.getIntent(), patient.getName(),
				patient.getPhone(), notification.getPayload());
		return new DeliveryResult(false, "No live channel configured - logged only");
	}

	@Override
	public boolean isAvailableFor(Patient patient) {
		return false; // no real channel is live yet in v1
	}
}