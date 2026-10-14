package com.furniture.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.furniture.entity.Expense;
import com.furniture.entity.ExpenseCategory;
import com.furniture.entity.PaymentStatus;
import com.furniture.entity.Project;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.ExpenseRepository;
import com.furniture.repository.PaymentRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ProjectRepository projectRepository;
    private final PaymentRepository paymentRepository;

    public ExpenseService(ExpenseRepository expenseRepository, ProjectRepository projectRepository,PaymentRepository paymentRepository){
        this.expenseRepository = expenseRepository;
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
    }
    public Expense addExpense(Expense expense){
        if (expense.getProject() == null || expense.getProject().getId() == null) {
            throw new BadRequestException("project.id is required");
        }
        if (expense.getAmount() == null) {
            throw new BadRequestException("amount is required");
        }
        if (expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be greater than 0");
        }
        if (expense.getCategory() == null) {
            throw new BadRequestException("category is required");
        }
        if (expense.getExpenseDate() == null) {
            throw new BadRequestException("expenseDate is required");
        }
        if (expense.getExpenseDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("expenseDate cannot be in the future");
        }
        if (expense.getDescription() != null && expense.getDescription().length() > 500) {
            throw new BadRequestException("description must be at most 500 characters");
        }

        Long projectId = expense.getProject().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        expense.setId(null);
        expense.setProject(project);

        return expenseRepository.save(expense);
    }
    public List<Expense> getAllExpense() {
        return expenseRepository.findAll();
    }
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
    }
    public List<Expense> getExpenseByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        return expenseRepository.findByProjectId(projectId);
    }
    public List<Expense> findByProjectIdAndCategory(Long projectId, ExpenseCategory category){
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        return expenseRepository.findByProjectIdAndCategory(projectId, category);

    }
    public Map<String, Object> getProjectExpenseSummary(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        BigDecimal total = expenseRepository.sumAmountByProjectId(projectId);
        
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            byCategory.put(category.name(),
                    expenseRepository.sumAmountByProjectIdAndCategory(projectId, category));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("totalExpense", total);
        result.put("byCategory", byCategory);
        return result;
    }

    public Map<String, Object> getProjectProfit(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        BigDecimal revenue = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(projectId, PaymentStatus.PAID);
        BigDecimal expense = expenseRepository.sumAmountByProjectId(projectId);
        BigDecimal profit = revenue.subtract(expense);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("revenue", revenue);
        result.put("expense", expense);
        result.put("profit", profit);
        return result;
    }
        
}
