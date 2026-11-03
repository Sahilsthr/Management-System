package com.furniture.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.AttendanceResponse;
import com.furniture.service.AttendanceService;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // GET ALL ATTENDANCE (ADMIN)
    @GetMapping
    public List<AttendanceResponse> getAllAttendance() {
        return attendanceService.getAllAttendance();
    }

    // GET ATTENDANCE BY ID (ADMIN)
    @GetMapping("/{id}")
    public AttendanceResponse getAttendanceById(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    // GET ATTENDANCE BY EMPLOYEE (ADMIN + EMPLOYEE)
    @GetMapping("/employee/{employeeId}")
    public List<AttendanceResponse> getAttendanceByEmployee(
            @PathVariable Long employeeId,
            Authentication authentication) {

        return attendanceService.getAttendanceByEmployee(
                employeeId,
                authentication.getName()
        );
    }

    // GET ATTENDANCE BY PROJECT (ADMIN)
    @GetMapping("/project/{projectId}")
    public List<AttendanceResponse> getAttendanceByProject(
            @PathVariable Long projectId) {

        return attendanceService.getAttendanceByProject(projectId);
    }

    // GET ATTENDANCE BY DATE (ADMIN)
    @GetMapping("/date/{date}")
    public List<AttendanceResponse> getAttendanceByDate(
            @PathVariable LocalDate date) {

        return attendanceService.getAttendanceByDate(date);
    }
}