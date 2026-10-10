package com.furniture.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.entity.Expense;
import com.furniture.entity.ExpenseCategory;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByProjectId(Long projectId);

    List<Expense> findByProjectIdAndCategory(Long projectId, ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
        "WHERE e.project.id = :projectId")
    BigDecimal sumAmountByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
        "WHERE e.project.id = :projectId AND e.category = :category")
    BigDecimal sumAmountByProjectIdAndCategory(
            @Param("projectId") Long projectId,
            @Param("category") ExpenseCategory category);
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
        BigDecimal sumAllExpenses();

}
