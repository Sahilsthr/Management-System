package com.furniture.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.AttendanceResponse;
import com.furniture.entity.Attendance;
import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceStatus;
import com.furniture.entity.User;
import com.furniture.exception.DuplicateResourceException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.AttendanceRepository;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.UserRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {

        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // CREATE ATTENDANCE FROM APPROVED REQUEST
    @Transactional
    public Attendance createFromRequest(AttendanceRequest request) {

        boolean exists = attendanceRepository
                .existsByEmployeeIdAndProjectIdAndAttendanceDate(
                        request.getEmployee().getId(),
                        request.getProject().getId(),
                        request.getAttendanceDate());

        if (exists) {
            throw new DuplicateResourceException(
                    "Attendance already exists for this employee, project and date");
        }

        Attendance attendance = new Attendance();

        attendance.setEmployee(request.getEmployee());
        attendance.setProject(request.getProject());
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setAttendanceStatus(AttendanceStatus.PRESENT);
        attendance.setAttendanceRequest(request);

        return attendanceRepository.save(attendance);
    }

    // CONVERT ENTITY TO DTO
    private AttendanceResponse mapToResponse(Attendance attendance) {

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEmployee().getUser().getName(),
                attendance.getEmployee().getEmployeeCode(),
                attendance.getProject().getProjectName(),
                attendance.getAttendanceDate(),
                attendance.getAttendanceStatus(),
                attendance.getMarkedAt()
        );
    }

    // GET ALL ATTENDANCE (ADMIN)
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAllAttendance() {

        return attendanceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ATTENDANCE BY ID (ADMIN)
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance not found with id " + id));

        return mapToResponse(attendance);
    }

    // GET ATTENDANCE BY EMPLOYEE
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByEmployee(
            Long employeeId, String email) {

        User currentUser = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // ADMIN can view any employee's attendance
        if (!currentUser.getRole().name().equals("ADMIN")) {

            // EMPLOYEE can view only their own attendance
            if (!currentUser.getRole().name().equals("EMPLOYEE")) {
                throw new AccessDeniedException(
                        "You do not have permission to view attendance");
            }

            Long currentEmployeeId = employeeRepository
                    .findByUser_Id(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee profile not found"))
                    .getId();

            if (!currentEmployeeId.equals(employeeId)) {
                throw new AccessDeniedException(
                        "You can only view your own attendance");
            }
        }

        return attendanceRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ATTENDANCE BY PROJECT (ADMIN)
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByProject(Long projectId) {

        return attendanceRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET ATTENDANCE BY DATE (ADMIN)
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {

        return attendanceRepository.findByAttendanceDate(date)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}