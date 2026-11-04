
package com.furniture.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateExpenseRequest;
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

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ProjectRepository projectRepository,
            PaymentRepository paymentRepository) {
        this.expenseRepository = expenseRepository;
        this.projectRepository = projectRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Expense addExpense(CreateExpenseRequest request) {

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be greater than zero");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + request.getProjectId()));

        Expense expense = new Expense();
        expense.setProject(project);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> getAllExpense() {
        return expenseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpenseByProject(Long projectId) {

        validateProject(projectId);

        return expenseRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Expense> findByProjectIdAndCategory(
            Long projectId, ExpenseCategory category) {

        validateProject(projectId);

        return expenseRepository.findByProjectIdAndCategory(
                projectId, category);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectExpenseSummary(Long projectId) {

        validateProject(projectId);

        BigDecimal total = expenseRepository.sumAmountByProjectId(projectId);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();

        for (ExpenseCategory category : ExpenseCategory.values()) {
            BigDecimal categoryTotal =
                    expenseRepository.sumAmountByProjectIdAndCategory(
                            projectId, category);

            byCategory.put(
                    category.name(),
                    categoryTotal != null ? categoryTotal : BigDecimal.ZERO);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("totalExpense",
                total != null ? total : BigDecimal.ZERO);
        result.put("byCategory", byCategory);

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectProfit(Long projectId) {

        validateProject(projectId);

        BigDecimal revenue = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(
                        projectId, PaymentStatus.PAID);

        BigDecimal expense = expenseRepository
                .sumAmountByProjectId(projectId);

        revenue = revenue != null ? revenue : BigDecimal.ZERO;
        expense = expense != null ? expense : BigDecimal.ZERO;

        BigDecimal profit = revenue.subtract(expense);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("revenue", revenue);
        result.put("expense", expense);
        result.put("profit", profit);

        return result;
    }

    private void validateProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(
                    "Project not found with id " + projectId);
        }
    }
}
