package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.Feedback;
import com.gatistack.siletry.repository.AppointmentRepository;
import com.gatistack.siletry.repository.FeedbackRepository;
import com.gatistack.siletry.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

	private final AppointmentRepository appointmentRepository;
	private final FeedbackRepository feedbackRepository;
	private final NotificationRepository notificationRepository;

	public AnalyticsService(AppointmentRepository appointmentRepository, FeedbackRepository feedbackRepository,
			NotificationRepository notificationRepository) {
		this.appointmentRepository = appointmentRepository;
		this.feedbackRepository = feedbackRepository;
		this.notificationRepository = notificationRepository;
	}

	public record DashboardSummary(long totalAppointments, long completedAppointments, long noShowAppointments,
			double noShowRate, double averageRating, Map<String, Long> sentimentBreakdown,
			long escalatedFeedbackCount) {
	}

	public DashboardSummary getSummary(LocalDateTime start, LocalDateTime end) {
		long total = appointmentRepository.countByScheduledAtBetween(start, end);
		long completed = appointmentRepository
				.countByStatusAndScheduledAtBetween(Appointment.AppointmentStatus.COMPLETED, start, end);
		long noShows = appointmentRepository.countByStatusAndScheduledAtBetween(Appointment.AppointmentStatus.NO_SHOW,
				start, end);

		double noShowRate = total > 0 ? (double) noShows / total * 100 : 0.0;

		List<Feedback> feedbackInRange = feedbackRepository.findByCreatedAtBetween(start, end);

		double avgRating = feedbackInRange.stream().mapToInt(Feedback::getRating).average().orElse(0.0);

		Map<String, Long> sentimentBreakdown = feedbackInRange.stream().filter(f -> f.getSentiment() != null)
				.collect(Collectors.groupingBy(f -> f.getSentiment().name(), Collectors.counting()));

		long escalatedCount = feedbackInRange.stream().filter(Feedback::isEscalated).count();

		return new DashboardSummary(total, completed, noShows, noShowRate, avgRating, sentimentBreakdown,
				escalatedCount);
	}
}