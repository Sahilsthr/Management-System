package com.furniture.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.Expense;
import com.furniture.entity.ExpenseCategory;
import com.furniture.service.ExpenseService;

@RestController
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @PostMapping("/api/expenses")
    public Expense addExpense(@RequestBody Expense expense) {
        return expenseService.addExpense(expense);
    }

    @GetMapping("/api/expenses")
    public List<Expense> getAllExpense() {
        return expenseService.getAllExpense();
    }
    @GetMapping("/api/expenses/{id}")
    public Optional<Expense> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @GetMapping("/api/projects/{projectId}/expenses")
    public List<Expense> getExpenseByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) ExpenseCategory category) {
        if (category == null) {
            return expenseService.getExpenseByProject(projectId);
        }
        return expenseService.findByProjectIdAndCategory(projectId, category);
    }
    @GetMapping("/api/projects/{projectId}/expense-summary")
    public Map<String, Object> getProjectExpenseSummary(@PathVariable Long projectId) {
        return expenseService.getProjectExpenseSummary(projectId);
    }
    @GetMapping("/api/projects/{projectId}/profit")
    public Map<String, Object> getProjectProfit(@PathVariable Long projectId) {
        return expenseService.getProjectProfit(projectId);
    }

    
}
