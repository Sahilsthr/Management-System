package com.furniture.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Attendance;
import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceStatus;
import com.furniture.repository.AttendanceRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance createFromRequest(AttendanceRequest request) {

        boolean exists = attendanceRepository
                .existsByEmployeeIdAndProjectIdAndAttendanceDate(
                        request.getEmployee().getId(),
                        request.getProject().getId(),
                        request.getAttendanceDate());

        if (exists) {
            throw new RuntimeException(
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

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAttendanceByProject(Long projectId) {
        return attendanceRepository.findByProjectId(projectId);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date);
    }
}