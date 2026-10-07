package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.entity.Employee;
import com.furniture.entity.User;
import com.furniture.repository.AttendanceRequestRepository;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.UserRepository;

@Service
public class AttendanceRequestService {

    private final AttendanceRequestRepository attendanceRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public AttendanceRequestService(AttendanceRequestRepository attendanceRequestRepository,
                                    EmployeeRepository employeeRepository,
                                    UserRepository userRepository) {
        this.attendanceRequestRepository = attendanceRequestRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    public AttendanceRequest createRequest(AttendanceRequest ar) {
        Employee employee = employeeRepository.findById(ar.getEmployee().getId()).orElse(null);
        ar.setEmployee(employee);
        return attendanceRequestRepository.save(ar);
    }

    public AttendanceRequest approveRequest(Long id, Long reviewerId, String remark) {
        return reviewRequest(id, reviewerId, remark, AttendanceRequestStatus.APPROVED);
    }

    public AttendanceRequest rejectRequest(Long id, Long reviewerId, String remark) {
        return reviewRequest(id, reviewerId, remark, AttendanceRequestStatus.REJECTED);
    }

    private AttendanceRequest reviewRequest(Long id, Long reviewerId, String remark,
                                            AttendanceRequestStatus status) {
        AttendanceRequest ar = attendanceRequestRepository.findById(id).orElse(null);
        if (ar == null) {
            return null;
        }

        User reviewer = userRepository.findById(reviewerId).orElse(null);
        if (reviewer == null) {
            return ar;
        }

        ar.setAttendanceRequestStatus(status);
        ar.setRemark(remark);
        ar.setReviewedBy(reviewer);
        ar.setReviewedAt(LocalDateTime.now());
        return attendanceRequestRepository.save(ar);
    }

    public List<AttendanceRequest> getAllRequests() {
        return attendanceRequestRepository.findAll();
    }

    public Optional<AttendanceRequest> getRequestById(Long id) {
        return attendanceRequestRepository.findById(id);
    }

    public List<AttendanceRequest> getRequestsByEmployee(Long employeeId) {
        return attendanceRequestRepository.findByEmployeeId(employeeId);
    }

    public List<AttendanceRequest> getRequestsByStatus(AttendanceRequestStatus status) {
        return attendanceRequestRepository.findByAttendanceRequestStatus(status);
    }
}