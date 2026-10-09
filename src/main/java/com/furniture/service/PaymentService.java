package com.furniture.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.Payment;
import com.furniture.entity.PaymentStatus;
import com.furniture.entity.Project;
import com.furniture.repository.PaymentRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;

    public PaymentService(PaymentRepository paymentRepository,ProjectRepository projectRepository){
        this.paymentRepository = paymentRepository;
        this.projectRepository = projectRepository;
    }

    public Payment addPayment(Payment payment){
        Project project =  projectRepository.findById(payment.getProject().getId()).orElse(null);
        payment.setProject(project);

        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Long id, PaymentStatus status){
        Payment payment = paymentRepository.findById(id).orElse(null);
        payment.setPaymentStatus(status);

        return paymentRepository.save(payment);
    }
    public List<Payment> getAllPayment() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentByProject(Long projectId) {
        return paymentRepository.findByProjectId(projectId);
    }

    public Map<String, Object> getProjectPaymentSummary(Long projectId){
        Project project = projectRepository.findById(projectId).orElse(null);
        
        BigDecimal total = project.getTotalAmount();
        if(total == null){
            total = BigDecimal.ZERO;
        }
        BigDecimal paid = paymentRepository.sumAmountByProjectIdAndPaymentStatus(projectId, PaymentStatus.PAID);

        BigDecimal pending = total.subtract(paid);

        return Map.of(
            "projectId", projectId,
            "totalAmount", total,
            "paidAmount", paid,
            "pendingAmount", pending
        );
    }

}