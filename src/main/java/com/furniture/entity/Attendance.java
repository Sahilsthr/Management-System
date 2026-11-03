
package com.furniture.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

    @Entity
    @Table(
        name = "attendances",
        uniqueConstraints = {
            @UniqueConstraint(
                name = "uk_attendance_employee_project_date",
                columnNames = {
                    "employee_id",
                    "project_id",
                    "attendance_date"
                }
            ),
            @UniqueConstraint(
                name = "uk_attendance_request",
                columnNames = "attendance_request_id"
            )
        }
    )
    public class Attendance {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id", nullable = false)
        private Employee employee;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "project_id", nullable = false)
        private Project project;

        @Column(name = "attendance_date", nullable = false)
        private LocalDate attendanceDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "attendance_status", nullable = false)
        private AttendanceStatus attendanceStatus;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "attendance_request_id", unique = true)
        private AttendanceRequest attendanceRequest;

        @Column(nullable = false, updatable = false)
        private LocalDateTime markedAt;

        public Attendance() {
        }

        @PrePersist
        public void onCreate() {
            if (this.markedAt == null) {
                this.markedAt = LocalDateTime.now();
            }
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Employee getEmployee() {
            return employee;
        }

        public void setEmployee(Employee employee) {
            this.employee = employee;
        }

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public LocalDate getAttendanceDate() {
            return attendanceDate;
        }

        public void setAttendanceDate(LocalDate attendanceDate) {
            this.attendanceDate = attendanceDate;
        }

        public AttendanceStatus getAttendanceStatus() {
            return attendanceStatus;
        }

        public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
            this.attendanceStatus = attendanceStatus;
        }

        public AttendanceRequest getAttendanceRequest() {
            return attendanceRequest;
        }

        public void setAttendanceRequest(AttendanceRequest attendanceRequest) {
            this.attendanceRequest = attendanceRequest;
        }

        public LocalDateTime getMarkedAt() {
            return markedAt;
        }

        public void setMarkedAt(LocalDateTime markedAt) {
            this.markedAt = markedAt;
        }
    }
