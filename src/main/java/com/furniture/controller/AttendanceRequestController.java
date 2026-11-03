package com.furniture.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.AttendanceRequestResponse;
import com.furniture.dto.CreateAttendanceRequestDto;
import com.furniture.dto.ReviewAttendanceRequestDto;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.service.AttendanceRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance-requests")
public class AttendanceRequestController {

    private final AttendanceRequestService attendanceRequestService;

    public AttendanceRequestController(
            AttendanceRequestService attendanceRequestService) {
        this.attendanceRequestService = attendanceRequestService;
    }

    // CREATE ATTENDANCE REQUEST
    @PostMapping
    public AttendanceRequestResponse createRequest(
            @Valid @RequestBody CreateAttendanceRequestDto request,
            Authentication authentication) {

        return attendanceRequestService.createRequest(
                request, authentication.getName());
    }

    // GET ALL REQUESTS (ADMIN)
    @GetMapping
    public List<AttendanceRequestResponse> getAllRequests() {
        return attendanceRequestService.getAllRequests();
    }

    // GET REQUEST BY ID (ADMIN)
    @GetMapping("/{id}")
    public AttendanceRequestResponse getRequestById(
            @PathVariable Long id) {

        return attendanceRequestService.getRequestById(id);
    }

    // GET PENDING REQUESTS (ADMIN)
    @GetMapping("/pending")
    public List<AttendanceRequestResponse> getPendingRequests() {

        return attendanceRequestService.getRequestsByStatus(
                AttendanceRequestStatus.PENDING);
    }

    // GET REQUESTS BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRequestResponse> getRequestsByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {

        return attendanceRequestService.getRequestsByEmployee(
                employeeId, authentication.getName());
    }

    // APPROVE REQUEST (ADMIN)
    @PutMapping("/{id}/approve")
    public AttendanceRequestResponse approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewAttendanceRequestDto request,
            Authentication authentication) {

        String remark = request != null ? request.getRemark() : null;

        return attendanceRequestService.approveRequest(
                id, authentication.getName(), remark);
    }

    // REJECT REQUEST (ADMIN)
    @PutMapping("/{id}/reject")
    public AttendanceRequestResponse rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ReviewAttendanceRequestDto request,
            Authentication authentication) {

        String remark = request != null ? request.getRemark() : null;

        return attendanceRequestService.rejectRequest(
                id, authentication.getName(), remark);
    }
}