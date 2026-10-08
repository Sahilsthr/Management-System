package com.furniture.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furniture.entity.Payment;
import com.furniture.entity.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByProjectId(Long projectId);

    List<Payment> findByProjectIdAndPaymentStatus(Long projectId, PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
       "WHERE p.project.id = :projectId AND p.paymentStatus = :paymentStatus")
    BigDecimal sumAmountByProjectIdAndPaymentStatus(@Param("projectId") Long projectId,
            @Param("paymentStatus") PaymentStatus paymentStatus);
}
