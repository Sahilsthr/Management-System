package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.User;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.AttendanceRequestRepository;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.UserRepository;
import com.furniture.dto.CreateAttendanceRequestDto;


@Service
public class AttendanceRequestService {

    private final AttendanceRequestRepository attendanceRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final UserRepository userRepository;
    private final AttendanceService attendanceService;

    public AttendanceRequestService(AttendanceRequestRepository attendanceRequestRepository,
                                    EmployeeRepository employeeRepository,
                                    ProjectRepository projectRepository,
                                    ProjectEmployeeRepository projectEmployeeRepository,
                                    UserRepository userRepository,
                                AttendanceService attendanceService) {
        this.attendanceRequestRepository = attendanceRequestRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.projectEmployeeRepository = projectEmployeeRepository;
        this.userRepository = userRepository;
        this.attendanceService = attendanceService;
    }

    public AttendanceRequest createRequest(CreateAttendanceRequestDto request) {
        Long employeeId = request.getEmployeeId();
        Long projectId = request.getProjectId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id " + employeeId));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));

        if (!projectEmployeeRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new BadRequestException("Employee is not assigned to this project");
        }
        if (attendanceRequestRepository.existsByEmployeeIdAndProjectIdAndAttendanceDate(
                employeeId, projectId, request.getAttendanceDate())) {
            throw new DuplicateResourceException("Attendance request already exists for this date");
        }

        AttendanceRequest ar = new AttendanceRequest();
        ar.setEmployee(employee);
        ar.setProject(project);
        ar.setAttendanceDate(request.getAttendanceDate());
        ar.setAttendanceRequestStatus(AttendanceRequestStatus.PENDING);
        return attendanceRequestRepository.save(ar);
    }
    @Transactional
    public AttendanceRequest approveRequest(Long id, Long reviewerId, String remark) {
        return reviewRequest(id, reviewerId, remark, AttendanceRequestStatus.APPROVED);
    }

    public AttendanceRequest rejectRequest(Long id, Long reviewerId, String remark) {
        return reviewRequest(id, reviewerId, remark, AttendanceRequestStatus.REJECTED);
    }

    private AttendanceRequest reviewRequest(Long id, Long reviewerId, String remark,
                                            AttendanceRequestStatus status) {
        AttendanceRequest ar = attendanceRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attendance request not found with id " + id));

        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id " + reviewerId));

        if (!"ADMIN".equals(String.valueOf(reviewer.getRole()))) {
            throw new BadRequestException("Only an admin can review requests");
        }
        if (ar.getAttendanceRequestStatus() != AttendanceRequestStatus.PENDING) {
            throw new BadRequestException("Request is already " + ar.getAttendanceRequestStatus());
        }

        ar.setAttendanceRequestStatus(status);
        ar.setRemark(remark);
        ar.setReviewedBy(reviewer);
        ar.setReviewedAt(LocalDateTime.now());

        AttendanceRequest saved = attendanceRequestRepository.save(ar);
        if (status == AttendanceRequestStatus.APPROVED) {
            attendanceService.createFromRequest(saved);
        }
        return saved;
    }

    public List<AttendanceRequest> getAllRequests() {
        return attendanceRequestRepository.findAll();
    }

    public AttendanceRequest getRequestById(Long id) {
        return attendanceRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Attendance Request id not found by id "+ id));
    }

    public List<AttendanceRequest> getRequestsByEmployee(Long employeeId) {
        return attendanceRequestRepository.findByEmployeeId(employeeId);
    }

    public List<AttendanceRequest> getRequestsByStatus(AttendanceRequestStatus status) {
        return attendanceRequestRepository.findByAttendanceRequestStatus(status);
    }
}