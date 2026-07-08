package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.QueueEntry;
import com.gatistack.siletry.repository.QueueEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QueueService {

	private final QueueEntryRepository queueEntryRepository;

	public QueueService(QueueEntryRepository queueEntryRepository) {
		this.queueEntryRepository = queueEntryRepository;
	}

	public QueueEntry checkIn(Appointment appointment) {
		LocalDateTime dayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = dayStart.plusDays(1);

		int existingCount = queueEntryRepository
				.countByAppointment_Doctor_IdAndCheckedInAtBetween(appointment.getDoctor().getId(), dayStart, dayEnd);

		QueueEntry entry = new QueueEntry();
		entry.setAppointment(appointment);
		entry.setTokenNumber(existingCount + 1); // simple sequential numbering per doctor per day
		return queueEntryRepository.save(entry);
	}

	public List<QueueEntry> todaysQueue(String doctorId) {
		LocalDateTime dayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime dayEnd = dayStart.plusDays(1);
		return queueEntryRepository.findByAppointment_Doctor_IdAndCheckedInAtBetweenOrderByTokenNumberAsc(doctorId,
				dayStart, dayEnd);
	}

	public QueueEntry updateStatus(String queueEntryId, QueueEntry.QueueStatus status) {
		QueueEntry entry = queueEntryRepository.findById(queueEntryId)
				.orElseThrow(() -> new IllegalArgumentException("Queue entry not found: " + queueEntryId));
		entry.setStatus(status);
		if (status == QueueEntry.QueueStatus.CALLED)
			entry.setCalledAt(LocalDateTime.now());
		if (status == QueueEntry.QueueStatus.COMPLETED)
			entry.setCompletedAt(LocalDateTime.now());
		return queueEntryRepository.save(entry);
	}
}