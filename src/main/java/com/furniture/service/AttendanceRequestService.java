
package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateAttendanceRequestDto;
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

@Service
public class AttendanceRequestService {

    private final AttendanceRequestRepository attendanceRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final UserRepository userRepository;
    private final AttendanceService attendanceService;

    public AttendanceRequestService(
            AttendanceRequestRepository attendanceRequestRepository,
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

    private User getCurrentUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Logged-in user not found"));
    }

    private Employee getEmployeeForUser(User user) {
        return employeeRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee profile not found for logged-in user"));
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && "ADMIN".equals(user.getRole().name());
    }

    // CREATE ATTENDANCE REQUEST
    public AttendanceRequest createRequest(
            CreateAttendanceRequestDto request, String email) {

        User currentUser = getCurrentUser(email);

        Long employeeId;

        if (isAdmin(currentUser)) {
            employeeId = request.getEmployeeId();
        } else {
            employeeId = getEmployeeForUser(currentUser).getId();
        }

        Long projectId = request.getProjectId();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id " + employeeId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));

        if (!projectEmployeeRepository
                .existsByProjectIdAndEmployeeId(projectId, employeeId)) {
            throw new BadRequestException(
                    "Employee is not assigned to this project");
        }

        if (attendanceRequestRepository
                .existsByEmployeeIdAndProjectIdAndAttendanceDate(
                        employeeId, projectId, request.getAttendanceDate())) {
            throw new DuplicateResourceException(
                    "Attendance request already exists for this date");
        }

        AttendanceRequest ar = new AttendanceRequest();
        ar.setEmployee(employee);
        ar.setProject(project);
        ar.setAttendanceDate(request.getAttendanceDate());
        ar.setAttendanceRequestStatus(AttendanceRequestStatus.PENDING);

        return attendanceRequestRepository.save(ar);
    }

    // APPROVE ATTENDANCE REQUEST
    @Transactional
    public AttendanceRequest approveRequest(
            Long id, String email, String remark) {

        User reviewer = getCurrentUser(email);

        if (!isAdmin(reviewer)) {
            throw new BadRequestException(
                    "Only an admin can review requests");
        }

        return reviewRequest(
                id, reviewer, remark, AttendanceRequestStatus.APPROVED);
    }

    // REJECT ATTENDANCE REQUEST
    @Transactional
    public AttendanceRequest rejectRequest(
            Long id, String email, String remark) {

        User reviewer = getCurrentUser(email);

        if (!isAdmin(reviewer)) {
            throw new BadRequestException(
                    "Only an admin can review requests");
        }

        return reviewRequest(
                id, reviewer, remark, AttendanceRequestStatus.REJECTED);
    }

    // COMMON REVIEW METHOD
    private AttendanceRequest reviewRequest(
            Long id,
            User reviewer,
            String remark,
            AttendanceRequestStatus status) {

        AttendanceRequest ar = attendanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance request not found with id " + id));

        if (ar.getAttendanceRequestStatus()
                != AttendanceRequestStatus.PENDING) {
            throw new BadRequestException(
                    "Request is already " + ar.getAttendanceRequestStatus());
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

    // GET ALL REQUESTS
    public List<AttendanceRequest> getAllRequests() {
        return attendanceRequestRepository.findAll();
    }

    // GET REQUEST BY ID
    public AttendanceRequest getRequestById(Long id) {
        return attendanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance Request not found with id " + id));
    }

    // GET REQUESTS BY EMPLOYEE
    public List<AttendanceRequest> getRequestsByEmployee(
            Long employeeId, String email) {

        User currentUser = getCurrentUser(email);

        if (isAdmin(currentUser)) {
            return attendanceRequestRepository.findByEmployeeId(employeeId);
        }

        Employee employee = getEmployeeForUser(currentUser);

        if (!employee.getId().equals(employeeId)) {
            throw new BadRequestException(
                    "You can only view your own attendance requests");
        }

        return attendanceRequestRepository.findByEmployeeId(employee.getId());
    }

    // GET REQUESTS BY STATUS
    public List<AttendanceRequest> getRequestsByStatus(
            AttendanceRequestStatus status) {

        return attendanceRequestRepository
                .findByAttendanceRequestStatus(status);
    }
}
