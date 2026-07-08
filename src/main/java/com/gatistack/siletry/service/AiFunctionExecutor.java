package com.gatistack.siletry.service;

import com.gatistack.siletry.entity.Appointment;
import com.gatistack.siletry.entity.Doctor;
import com.gatistack.siletry.entity.Patient;
import com.gatistack.siletry.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiFunctionExecutor {

	private final BookingService bookingService;
	private final DoctorRepository doctorRepository;

	public AiFunctionExecutor(BookingService bookingService, DoctorRepository doctorRepository) {
		this.bookingService = bookingService;
		this.doctorRepository = doctorRepository;
	}

	// Tool definitions in OpenAI's function-calling schema format
	public List<Map<String, Object>> getToolDefinitions() {
		return List.of(
				tool("list_doctors", "List all active doctors at this clinic with their specializations",
						Map.of("type", "object", "properties", Map.of(), "required", List.of())),

				tool("check_availability", "Check available appointment slots for a doctor on a given date",
						Map.of("type", "object", "properties",
								Map.of("doctorId", Map.of("type", "string", "description", "The doctor's ID"), "date",
										Map.of("type", "string", "description", "Date in yyyy-MM-dd format")),
								"required", List.of("doctorId", "date"))),

				tool("book_appointment", "Book an appointment for the current patient", Map.of("type", "object",
						"properties",
						Map.of("doctorId", Map.of("type", "string", "description", "The doctor's ID"), "dateTime",
								Map.of("type", "string", "description", "ISO datetime, e.g. 2026-07-10T14:00:00"),
								"appointmentType",
								Map.of("type", "string", "enum", List.of("CONSULTATION", "FOLLOW_UP", "CHECKUP"))),
						"required", List.of("doctorId", "dateTime", "appointmentType"))),

				tool("reschedule_appointment", "Reschedule an existing appointment to a new time",
						Map.of("type", "object", "properties",
								Map.of("appointmentId", Map.of("type", "string", "description", "The appointment's ID"),
										"newDateTime",
										Map.of("type", "string", "description", "ISO datetime for the new slot")),
								"required", List.of("appointmentId", "newDateTime"))));
	}

	// Executes a tool call by name - this is the ONLY entry point the AI has into
	// real system actions. Every function here delegates to existing guarded
	// services (BookingService's conflict checking still applies) - the AI cannot
	// bypass any business logic, only trigger it.
	@SuppressWarnings("unchecked")
	public String execute(String functionName, Map<String, Object> arguments, Patient patient) {
		try {
			return switch (functionName) {
			case "list_doctors" -> listDoctors();
			case "check_availability" ->
				checkAvailability((String) arguments.get("doctorId"), (String) arguments.get("date"));
			case "book_appointment" -> bookAppointment(patient, (String) arguments.get("doctorId"),
					(String) arguments.get("dateTime"), (String) arguments.get("appointmentType"));
			case "reschedule_appointment" ->
				rescheduleAppointment((String) arguments.get("appointmentId"), (String) arguments.get("newDateTime"));
			default -> "Unknown function: " + functionName;
			};
		} catch (Exception e) {
			// Function failures must surface as text back to the AI (so it can tell the
			// patient something sensible went wrong), never as an unhandled exception
			// that crashes the whole conversation turn.
			return "Error executing " + functionName + ": " + e.getMessage();
		}
	}

	private String listDoctors() {
		List<Doctor> doctors = doctorRepository.findByActiveTrue();
		return doctors.stream().map(d -> d.getId() + ": Dr. " + d.getName() + " (" + d.getSpecialization() + ")")
				.collect(Collectors.joining("\n"));
	}

	private String checkAvailability(String doctorId, String date) {
		List<LocalDateTime> slots = bookingService.availableSlots(doctorId, LocalDate.parse(date));
		if (slots.isEmpty())
			return "No available slots on that date.";
		return slots.stream().map(LocalDateTime::toString).collect(Collectors.joining(", "));
	}

	private String bookAppointment(Patient patient, String doctorId, String dateTime, String appointmentType) {
		Appointment appointment = bookingService.book(patient, doctorId, LocalDateTime.parse(dateTime),
				Appointment.AppointmentType.valueOf(appointmentType), Appointment.BookingSource.WHATSAPP);
		return "Booked successfully. Appointment ID: " + appointment.getId() + " at " + appointment.getScheduledAt();
	}

	private String rescheduleAppointment(String appointmentId, String newDateTime) {
		Appointment appointment = bookingService.reschedule(appointmentId, LocalDateTime.parse(newDateTime));
		return "Rescheduled successfully to " + appointment.getScheduledAt();
	}

	private Map<String, Object> tool(String name, String description, Map<String, Object> parameters) {
		return Map.of("type", "function", "function",
				Map.of("name", name, "description", description, "parameters", parameters));
	}
}