package it.unimol.new_unimol.enrollments.service;

import it.unimol.new_unimol.enrollments.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    private List<CourseEnrollmentSettingsDto> settingList = new ArrayList<>();
    private List<CourseEnrollmentDto> enrollmentList = new ArrayList<>();
    private List<EnrollmentRequestDto> requestList = new ArrayList<>();


}
