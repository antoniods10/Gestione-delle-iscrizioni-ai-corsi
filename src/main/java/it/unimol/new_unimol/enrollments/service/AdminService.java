package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.*;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentSettingsDtoToCourseEnrollmentSettingsConverter;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentSettingsToCourseEnrollmentSettingsDtoConverter;
import it.unimol.new_unimol.enrollments.dto.converter.CourseEnrollmentToCourseEnrollmentDtoConverter;
import it.unimol.new_unimol.enrollments.dto.converter.EnrollmentRequestToEnrollmentRequestDtoConverter;
import it.unimol.new_unimol.enrollments.dto.rabbit.CourseValidationResponseDto;
import it.unimol.new_unimol.enrollments.model.CourseEnrollmentSettings;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
import it.unimol.new_unimol.enrollments.repository.EnrollmentRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AdminService {
    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private CourseEnrollmentSettingsToCourseEnrollmentSettingsDtoConverter settingsToDto;

    @Autowired
    private CourseEnrollmentSettingsDtoToCourseEnrollmentSettingsConverter dtoToSettings;

    @Autowired
    private CourseEnrollmentToCourseEnrollmentDtoConverter enrollmentToDto;

    @Autowired
    private EnrollmentRequestToEnrollmentRequestDtoConverter requestToDto;

    /**
     * Crea una nuova configurazione di iscrizione per un corso
     */
    public CourseEnrollmentSettingsDto createEnrollmentSettings(String courseId, CourseEnrollmentSettingsDto settingsDto, String adminId) {
        if(settingsRepository.existsByCourseId(courseId)) {
            throw new IllegalArgumentException("Esiste già una configurazione per il corso: " + courseId);
        }

        CourseValidationResponseDto courseValidation = rabbitMQService.validateCourse(courseId);
        if(!courseValidation.isValid()) {
            throw new IllegalArgumentException("Corso non valido: " + courseValidation.errorMessage());
        }

        if(settingsDto.enrollmentStartDate() != null && settingsDto.enrollmentEndDate() != null) {
            if(settingsDto.enrollmentStartDate().isAfter(settingsDto.enrollmentEndDate())) {
                throw new IllegalArgumentException("La data di inizio iscrizioni deve essere precedente alla data di fine");
            }
        }

        if(settingsDto.maxEnrollments() != null && settingsDto.maxEnrollments() <= 0) {
            throw new IllegalArgumentException("Il numero massimo di iscrizioni deve essere maggiore di 0");
        }

        CourseEnrollmentSettings settings = new CourseEnrollmentSettings();
        settings.setId(UUID.randomUUID().toString());
        settings.setCourseId(courseId);
        settings.setEnrollmentMode(settingsDto.enrollmentMode());
        settings.setRequiresApproval(settingsDto.requiresApproval());
        settings.setMaxEnrollments(settingsDto.maxEnrollments());
        settings.setEnrollmentStartDate(settingsDto.enrollmentStartDate());
        settings.setEnrollmentEndDate(settingsDto.enrollmentEndDate());
        settings.setAllowWaitingList(settingsDto.allowWaitingList());
        settings.setCreatedBy(adminId);
        settings.setCreatedDate(LocalDateTime.now());
        settings.setLastModifiedDate(LocalDateTime.now());

        CourseEnrollmentSettings saved = settingsRepository.save(settings);

        rabbitMQService.sendEnrollmentNotification(
                adminId,
                courseId,
                courseValidation.courseName(),
                "ENROLLMENT_SETTINGS_CREATED",
                "Configurazione iscrizione creata per il corso " + courseValidation.courseName(),
                Map.of("courseId", courseId, "enrollmentMode", settings.getEnrollmentMode().toString())
        );

        return settingsToDto.convert(saved);
    }

    /**
     * Restituisce tutte le configurazioni di iscrizione ad un corso
     */

}
