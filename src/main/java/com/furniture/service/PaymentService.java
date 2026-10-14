package com.furniture.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.furniture.entity.Payment;
import com.furniture.entity.PaymentStatus;
import com.furniture.entity.Project;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.PaymentRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.dto.CreatePaymentRequest;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;

    public PaymentService(PaymentRepository paymentRepository,
ProjectRepository projectRepository) {
        this.paymentRepository = paymentRepository;
        this.projectRepository = projectRepository;
    }

    public Payment addPayment(CreatePaymentRequest request) {
        
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + request.getProjectId()));

        PaymentStatus status = request.getPaymentStatus() != null
                ? request.getPaymentStatus()
                : PaymentStatus.PAID;

        if (status == PaymentStatus.PAID) {
            checkNotOverpaid(project, request.getAmount());
        }

        Payment payment = new Payment();
        payment.setProject(project);
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(status);
        payment.setRemark(request.getRemarks());
        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));

        if (status == PaymentStatus.PAID && payment.getPaymentStatus() != PaymentStatus.PAID) {
            checkNotOverpaid(payment.getProject(), payment.getAmount());
        }

        payment.setPaymentStatus(status);
        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayment() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));
    }

    public List<Payment> getPaymentByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
        return paymentRepository.findByProjectId(projectId);
    }

    public Map<String, Object> getProjectPaymentSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        BigDecimal total = project.getTotalAmount();
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        BigDecimal paid = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(projectId, PaymentStatus.PAID);
        BigDecimal pending = total.subtract(paid);

        return Map.of(
                "projectId", projectId,
                "totalAmount", total,
                "paidAmount", paid,
                "pendingAmount", pending);
    }

    // PAID payments ka total, project ke totalAmount se upar na jaye
    private void checkNotOverpaid(Project project, BigDecimal newAmount) {
        if (project.getTotalAmount() == null) {
            return;
        }
        BigDecimal alreadyPaid = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(project.getId(), PaymentStatus.PAID);
        if (alreadyPaid == null) {
            alreadyPaid = BigDecimal.ZERO;
        }
        if (alreadyPaid.add(newAmount).compareTo(project.getTotalAmount()) > 0) {
            throw new BadRequestException("Payment exceeds the project's total amount");
        }
    }
}