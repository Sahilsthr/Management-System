
package com.furniture.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreatePaymentRequest;
import com.furniture.entity.Payment;
import com.furniture.entity.PaymentStatus;
import com.furniture.entity.Project;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.PaymentRepository;
import com.furniture.repository.ProjectRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ProjectRepository projectRepository) {
        this.paymentRepository = paymentRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public Payment addPayment(CreatePaymentRequest request) {

        PaymentStatus status = request.getPaymentStatus() != null
                ? request.getPaymentStatus()
                : PaymentStatus.PAID;

        Project project;

        if (status == PaymentStatus.PAID) {
            project = getProjectForUpdate(request.getProjectId());
            checkNotOverpaid(project, request.getAmount());
        } else {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project not found with id " + request.getProjectId()));
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

    @Transactional
    public Payment updatePaymentStatus(Long id, PaymentStatus status) {

        if (status == null) {
            throw new BadRequestException("Payment status is required");
        }

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id " + id));

        if (status == PaymentStatus.PAID
                && payment.getPaymentStatus() != PaymentStatus.PAID) {

            Project project = getProjectForUpdate(payment.getProject().getId());

            checkNotOverpaid(project, payment.getAmount());
        }

        payment.setPaymentStatus(status);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getAllPayment() {
        return paymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentByProject(Long projectId) {

        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException(
                    "Project not found with id " + projectId);
        }

        return paymentRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectPaymentSummary(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));

        BigDecimal total = project.getTotalAmount() != null
                ? project.getTotalAmount()
                : BigDecimal.ZERO;

        BigDecimal paid = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(
                        projectId, PaymentStatus.PAID);

        BigDecimal pending = total.subtract(paid);

        return Map.of(
                "projectId", projectId,
                "totalAmount", total,
                "paidAmount", paid,
                "pendingAmount", pending);
    }

    private Project getProjectForUpdate(Long projectId) {

        return projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + projectId));
    }

    private void checkNotOverpaid(Project project, BigDecimal newAmount) {

        BigDecimal totalAmount = project.getTotalAmount();

        if (totalAmount == null
                || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Project total amount must be greater than zero before recording a paid payment");
        }

        BigDecimal alreadyPaid = paymentRepository
                .sumAmountByProjectIdAndPaymentStatus(
                        project.getId(), PaymentStatus.PAID);

        if (alreadyPaid == null) {
            alreadyPaid = BigDecimal.ZERO;
        }

        if (alreadyPaid.add(newAmount).compareTo(totalAmount) > 0) {
            throw new BadRequestException(
                    "Payment exceeds the project's total amount");
        }
    }
}
