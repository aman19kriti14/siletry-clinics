package com.gatistack.siletry.service.channel;

import com.gatistack.siletry.entity.NotificationRecord;
import com.gatistack.siletry.entity.Patient;

public interface ChannelAdapter {

	DeliveryResult send(NotificationRecord notification, Patient patient);

	boolean isAvailableFor(Patient patient);

	record DeliveryResult(boolean success, String message) {
	}
}