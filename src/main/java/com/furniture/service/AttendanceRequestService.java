
package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.AttendanceRequestResponse;
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

        // GET CURRENT USER
        private User getCurrentUser(String email) {
                return userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Logged-in user not found"));
        }

        // GET EMPLOYEE PROFILE
        private Employee getEmployeeForUser(User user) {
                return employeeRepository.findByUser_Id(user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Employee profile not found for logged-in user"));
        }

        // CHECK ADMIN ROLE
        private boolean isAdmin(User user) {
                return user.getRole() != null
                        && "ADMIN".equals(user.getRole().name());
        }

        // CONVERT ENTITY TO DTO
        private AttendanceRequestResponse mapToResponse(AttendanceRequest ar) {

                return new AttendanceRequestResponse(
                        ar.getId(),
                        ar.getEmployee().getUser().getName(),
                        ar.getEmployee().getEmployeeCode(),
                        ar.getAttendanceDate(),
                        ar.getAttendanceRequestStatus(),
                        ar.getProject().getProjectName(),
                        ar.getRequestedAt(),
                        ar.getReviewedAt(),
                        ar.getRemark()
                );
        }

        // CREATE ATTENDANCE REQUEST
        @Transactional
        public AttendanceRequestResponse createRequest(
                CreateAttendanceRequestDto request, String email) {

                User currentUser = getCurrentUser(email);

                Long employeeId;

                if (isAdmin(currentUser)) {

                if (request.getEmployeeId() == null) {
                        throw new BadRequestException(
                                "Employee ID is required for admin");
                }

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

                // CHECK PROJECT ASSIGNMENT
                if (!projectEmployeeRepository
                        .existsByProjectIdAndEmployeeId(projectId, employeeId)) {

                throw new BadRequestException(
                        "Employee is not assigned to this project");
                }

                // CHECK DUPLICATE REQUEST
                if (attendanceRequestRepository
                        .existsByEmployeeIdAndProjectIdAndAttendanceDate(
                                employeeId,
                                projectId,
                                request.getAttendanceDate())) {

                throw new DuplicateResourceException(
                        "Attendance request already exists for this date");
                }

                AttendanceRequest ar = new AttendanceRequest();

                ar.setEmployee(employee);
                ar.setProject(project);
                ar.setAttendanceDate(request.getAttendanceDate());
                ar.setAttendanceRequestStatus(AttendanceRequestStatus.PENDING);

                AttendanceRequest saved = attendanceRequestRepository.save(ar);

                return mapToResponse(saved);
        }

        // APPROVE ATTENDANCE REQUEST
        @Transactional
        public AttendanceRequestResponse approveRequest(
                Long id, String email, String remark) {

                User reviewer = getCurrentUser(email);

                if (!isAdmin(reviewer)) {
                throw new BadRequestException(
                        "Only an admin can review requests");
                }

                AttendanceRequest reviewed = reviewRequest(
                        id,
                        reviewer,
                        remark,
                        AttendanceRequestStatus.APPROVED);

                return mapToResponse(reviewed);
        }

        // REJECT ATTENDANCE REQUEST
        @Transactional
        public AttendanceRequestResponse rejectRequest(
                Long id, String email, String remark) {

                User reviewer = getCurrentUser(email);

                if (!isAdmin(reviewer)) {
                throw new BadRequestException(
                        "Only an admin can review requests");
                }

                AttendanceRequest reviewed = reviewRequest(
                        id,
                        reviewer,
                        remark,
                        AttendanceRequestStatus.REJECTED);

                return mapToResponse(reviewed);
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
                        "Request is already "
                                + ar.getAttendanceRequestStatus());
                }

                ar.setAttendanceRequestStatus(status);
                ar.setRemark(remark);
                ar.setReviewedBy(reviewer);
                ar.setReviewedAt(LocalDateTime.now());

                AttendanceRequest saved = attendanceRequestRepository.save(ar);

                // CREATE ATTENDANCE ONLY AFTER APPROVAL
                if (status == AttendanceRequestStatus.APPROVED) {
                attendanceService.createFromRequest(saved);
                }

                return saved;
        }

        // GET ALL REQUESTS
        @Transactional(readOnly = true)
        public List<AttendanceRequestResponse> getAllRequests() {

                return attendanceRequestRepository.findAll()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();
        }

        // GET REQUEST BY ID
        @Transactional(readOnly = true)
        public AttendanceRequestResponse getRequestById(Long id) {

                AttendanceRequest ar = attendanceRequestRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Attendance request not found with id " + id));

                return mapToResponse(ar);
        }

        // GET REQUESTS BY EMPLOYEE
        @Transactional(readOnly = true)
        public List<AttendanceRequestResponse> getRequestsByEmployee(
                Long employeeId, String email) {

                User currentUser = getCurrentUser(email);

                List<AttendanceRequest> requests;

                if (isAdmin(currentUser)) {

                requests = attendanceRequestRepository
                        .findByEmployeeId(employeeId);

                } else {

                Employee employee = getEmployeeForUser(currentUser);

                if (!employee.getId().equals(employeeId)) {
                        throw new BadRequestException(
                                "You can only view your own attendance requests");
                }

                requests = attendanceRequestRepository
                        .findByEmployeeId(employee.getId());
                }

                return requests.stream()
                        .map(this::mapToResponse)
                        .toList();
        }

        // GET REQUESTS BY STATUS
        @Transactional(readOnly = true)
        public List<AttendanceRequestResponse> getRequestsByStatus(
                AttendanceRequestStatus status) {

                return attendanceRequestRepository
                        .findByAttendanceRequestStatus(status)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();
        }
}
