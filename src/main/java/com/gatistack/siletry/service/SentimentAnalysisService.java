package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Feedback;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SentimentAnalysisService {

	// Deliberately simple keyword-based approach for v1 - upgrading to real
	// NLP/LLM-based
	// sentiment analysis is a known future improvement once an AI provider key is
	// wired in,
	// not something to silently fake here.
	private static final List<String> NEGATIVE_WORDS = List.of("bad", "terrible", "worst", "rude", "waited",
			"long wait", "unprofessional", "dirty", "never again", "disappointed", "poor", "horrible");

	public Feedback.Sentiment analyze(Integer rating, String comment) {
		if (rating != null && rating <= 2) {
			return Feedback.Sentiment.NEGATIVE;
		}
		if (rating != null && rating >= 4) {
			return Feedback.Sentiment.POSITIVE;
		}

		if (comment != null) {
			String lower = comment.toLowerCase(Locale.ROOT);
			boolean hasNegativeWord = NEGATIVE_WORDS.stream().anyMatch(lower::contains);
			if (hasNegativeWord) {
				return Feedback.Sentiment.NEGATIVE;
			}
		}

		return Feedback.Sentiment.NEUTRAL;
	}
}