package com.furniture.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.entity.AttendanceRequest;
import com.furniture.entity.AttendanceRequestStatus;

public interface AttendanceRequestRepository extends JpaRepository<AttendanceRequest, Long> {

    List<AttendanceRequest> findByEmployeeId(Long employeeId);

    List<AttendanceRequest> findByAttendanceRequestStatus(AttendanceRequestStatus attendanceRequestStatus);

    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
}