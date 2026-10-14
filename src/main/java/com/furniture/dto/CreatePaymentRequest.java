package com.furniture.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import com.furniture.entity.PaymentMethod;
import com.furniture.entity.PaymentStatus;



public class CreatePaymentRequest {
    @NotNull(message = "Project id is required!")
    private Long projectId;
    
    @NotNull(message = "Amount is required")
    @Positive (message= " Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message="payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment method is Required")
    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    @Size(max = 255, message = "Remark must be at most 255 characters")
    private String remarks;

    public CreatePaymentRequest() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long id) {
        this.projectId = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    

    
}
