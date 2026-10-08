package com.furniture.controller;

import org.springframework.web.bind.annotation.*;
import com.furniture.entity.Attendance;

import java.time.LocalDate;
import java.util.*;
import com.furniture.service.AttendanceService;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService){
        this.attendanceService = attendanceService;

    }
    @GetMapping
    public List<Attendance> getAllAttendance(){
        return attendanceService.getAllAttendance();
    }
    @GetMapping("/{id}")
    public Optional<Attendance> getAttendanceById(@PathVariable Long id){
        return attendanceService.getAttendanceById(id);

    }
    @GetMapping("/employee/{employeeId}")
    public List<Attendance> getAttendanceByEmployee(@PathVariable Long employeeId){
        return attendanceService.getAttendanceByEmployee(employeeId);
    }
    @GetMapping("/project/{projectId}")
    public List<Attendance> getAttendanceByProject(@PathVariable Long projectId){
        return attendanceService.getAttendanceByProject(projectId);
    }
    @GetMapping("date/{date}")
    public List<Attendance> getAttendanceByDate(@PathVariable LocalDate date){
        return attendanceService.getAttendanceByDate(date);
    }





    
}
