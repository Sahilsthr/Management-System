package com.furniture.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmployeeId(Long employeeId);

    List<Attendance> findByProjectId(Long projectId);

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    Optional<Attendance> findByEmployeeIdAndProjectIdAndAttendanceDate(
            Long employeeId, Long projectId, LocalDate attendanceDate);

    boolean existsByEmployeeIdAndProjectIdAndAttendanceDate(
            Long employeeId, Long projectId, LocalDate attendanceDate);
}