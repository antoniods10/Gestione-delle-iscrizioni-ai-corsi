package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.*;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentRepository;
import it.unimol.new_unimol.enrollments.repository.CourseEnrollmentSettingsRepository;
import it.unimol.new_unimol.enrollments.repository.EnrollmentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    @Autowired
    private CourseEnrollmentSettingsRepository settingsRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private EnrollmentRequestRepository requestRepository;

    //Mancano le cose per rabbit

    public CourseEnrollmentSettingsDto createEnrollmentSettings(CourseEnrollmentSettingsDto settingsDto) {

    }


}
