package com.furniture.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.furniture.entity.ExpenseCategory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateExpenseRequest {

    @NotNull(message = "Project id is required")
    private Long projectId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private ExpenseCategory category;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;

    public CreateExpenseRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    
    }

    public BigDecimal getAmount() {
        return amount; 

    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;

    }

    public ExpenseCategory getCategory() {
        return category; 

    }
    public void setCategory(ExpenseCategory category) {
        this.category = category;

    }

    public String getDescription() {
        return description;

    }
    public void setDescription(String description) {
        this.description = description;

    }
    public LocalDate getExpenseDate() {
        return expenseDate;
    }
    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;

    }
}
