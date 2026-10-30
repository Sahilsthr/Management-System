package com.furniture.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.CreateAttendanceRequestDto;
import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.service.AttendanceRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance-requests")
public class AttendanceRequestController {

    private final AttendanceRequestService attendanceRequestService;

    public AttendanceRequestController(AttendanceRequestService attendanceRequestService) {
        this.attendanceRequestService = attendanceRequestService;
    }

    @PostMapping
    public AttendanceRequest createRequest(
            @Valid @RequestBody CreateAttendanceRequestDto request,
            Authentication authentication) {

        return attendanceRequestService.createRequest(
                request, authentication.getName());
    }

    @GetMapping
    public List<AttendanceRequest> getAllRequests() {
        return attendanceRequestService.getAllRequests();
    }

    @GetMapping("/{id}")
    public AttendanceRequest getRequestById(@PathVariable Long id) {
        return attendanceRequestService.getRequestById(id);
    }

    @GetMapping("/pending")
    public List<AttendanceRequest> getPendingRequests() {
        return attendanceRequestService.getRequestsByStatus(AttendanceRequestStatus.PENDING);
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRequest> getRequestsByEmployee(@PathVariable Long employeeId, Authentication authentication) {
        return attendanceRequestService.getRequestsByEmployee(employeeId, authentication.getName());
    }

    @PutMapping("/{id}/approve")
    public AttendanceRequest approveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String remark, Authentication authentication) {
        return attendanceRequestService.approveRequest(id, authentication.getName(), remark);
    }

    @PutMapping("/{id}/reject")
    public AttendanceRequest rejectRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String remark,
            Authentication authentication) {

        return attendanceRequestService.rejectRequest(
                id, authentication.getName(), remark);
    }
}