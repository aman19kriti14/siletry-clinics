package com.gatistack.siletry.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.Feedback;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.entity.Feedback.Sentiment;
import com.gatistack.siletry.repository.FeedbackRepository;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SentimentAnalysisService sentimentAnalysisService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                            SentimentAnalysisService sentimentAnalysisService) {
        this.feedbackRepository = feedbackRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
    }

    public Feedback submit(Patient patient, Appointment appointment, Integer rating, String comment) {
        Feedback feedback = new Feedback();
        feedback.setPatient(patient);
        feedback.setAppointment(appointment);
        feedback.setRating(rating);
        feedback.setComment(comment);

        Feedback.Sentiment sentiment = sentimentAnalysisService.analyze(rating, comment);
        feedback.setSentiment(sentiment);
        feedback.setEscalated(sentiment == Feedback.Sentiment.NEGATIVE);

        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getEscalated() {
        return feedbackRepository.findByEscalatedTrueOrderByCreatedAtDesc();
    }
}