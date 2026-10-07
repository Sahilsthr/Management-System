package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.service.AttendanceRequestService;

@RestController
@RequestMapping("/api/attendance-requests")
public class AttendanceRequestController {

    private final AttendanceRequestService attendanceRequestService;

    public AttendanceRequestController(AttendanceRequestService attendanceRequestService) {
        this.attendanceRequestService = attendanceRequestService;
    }

    @PostMapping
    public AttendanceRequest createRequest(@RequestBody AttendanceRequest ar) {
        return attendanceRequestService.createRequest(ar);
    }

    @GetMapping
    public List<AttendanceRequest> getAllRequests() {
        return attendanceRequestService.getAllRequests();
    }

    @GetMapping("/{id}")
    public Optional<AttendanceRequest> getRequestById(@PathVariable Long id) {
        return attendanceRequestService.getRequestById(id);
    }

    @GetMapping("/pending")
    public List<AttendanceRequest> getPendingRequests() {
        return attendanceRequestService.getRequestsByStatus(AttendanceRequestStatus.PENDING);
    }

    @GetMapping("/employee/{employeeId}")
    public List<AttendanceRequest> getRequestsByEmployee(@PathVariable Long employeeId) {
        return attendanceRequestService.getRequestsByEmployee(employeeId);
    }

    @PutMapping("/{id}/approve")
    public AttendanceRequest approveRequest(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String remark) {
        return attendanceRequestService.approveRequest(id, reviewerId, remark);
    }

    @PutMapping("/{id}/reject")
    public AttendanceRequest rejectRequest(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @RequestParam(required = false) String remark) {
        return attendanceRequestService.rejectRequest(id, reviewerId, remark);
    }
}