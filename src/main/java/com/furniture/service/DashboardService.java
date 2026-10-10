package com.furniture.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.furniture.entity.ProjectStatus;
import com.furniture.entity.PaymentStatus;
import com.furniture.entity.Project;
import com.furniture.entity.AttendanceRequestStatus;
import com.furniture.repository.AttendanceRequestRepository;
import com.furniture.repository.ClientRepository;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ExpenseRepository;
import com.furniture.repository.PaymentRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class DashboardService {
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AttendanceRequestRepository attendanceRequestRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final WorkTaskService workTaskService;
    private final PaymentService paymentService;

    public DashboardService(ClientRepository clientRepository,
                            EmployeeRepository employeeRepository,
                            ProjectRepository projectRepository,
                            AttendanceRequestRepository attendanceRequestRepository,
                            PaymentRepository paymentRepository,
                            ExpenseRepository expenseRepository,
                            ExpenseService expenseService,
                            WorkTaskService workTaskService,
                            PaymentService paymentService) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.attendanceRequestRepository = attendanceRequestRepository;
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.workTaskService = workTaskService;
        this.paymentService = paymentService;
    }

    public Map<String, Object> getAdminDashboard() {
        BigDecimal totalRevenue = paymentRepository.sumAllByPaymentStatus(PaymentStatus.PAID);
        BigDecimal totalExpenses = expenseRepository.sumAllExpenses();
        BigDecimal totalProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal totalProjectAmount = projectRepository.sumAllProjectAmounts();
        BigDecimal pendingPayments = totalProjectAmount.subtract(totalRevenue);

        Map<String, Object> adminDashboard = new LinkedHashMap<>();
        adminDashboard.put("totalClients", clientRepository.count());
        adminDashboard.put("totalEmployees", employeeRepository.count());
        adminDashboard.put("totalProjects", projectRepository.count());
        adminDashboard.put("activeProjects", projectRepository.countByProjectStatus(ProjectStatus.IN_PROGRESS));
        adminDashboard.put("pendingAttendanceRequests",
                attendanceRequestRepository.countByAttendanceRequestStatus(AttendanceRequestStatus.PENDING));
        adminDashboard.put("totalRevenue", totalRevenue);
        adminDashboard.put("totalExpenses", totalExpenses);
        adminDashboard.put("totalProfit", totalProfit);
        adminDashboard.put("pendingPayments", pendingPayments);

        return adminDashboard;
    }
    public Map<String, Object> getProjectDashboard(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);

        Map<String, Object> projectDashboard = new LinkedHashMap<>();
        projectDashboard.put("projectId", projectId);
        projectDashboard.put("projectName", project.getProjectName());
        projectDashboard.put("projectStatus", project.getProjectStatus());
        projectDashboard.put("progress", workTaskService.getProjectProgress(projectId));
        projectDashboard.put("payment", paymentService.getProjectPaymentSummary(projectId));
        projectDashboard.put("expense", expenseService.getProjectExpenseSummary(projectId));
        projectDashboard.put("profit", expenseService.getProjectProfit(projectId));

        return projectDashboard;
    }


    
}