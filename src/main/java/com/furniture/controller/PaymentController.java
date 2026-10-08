package com.furniture.controller;

import java.util.*;

import org.springframework.web.bind.annotation.RestController;

import com.furniture.service.PaymentService;
import com.furniture.entity.Payment;
import com.furniture.entity.PaymentStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping("/api/payments")
    public Payment addPayment(@RequestBody Payment payment) {
        return paymentService.addPayment(payment);
    }

    @GetMapping("/api/payments")
    public List<Payment> getAllPayment() {
        return paymentService.getAllPayment();
    }
    @GetMapping("/api/payments/{id}")
    public Optional<Payment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }
    @PutMapping("/api/payments/{id}/status")
    public Payment updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return paymentService.updatePaymentStatus(id, status);
    }
    @GetMapping("/api/projects/{projectId}/payments")
    public List<Payment> getPaymentByProject(@PathVariable Long projectId) {
        return paymentService.getPaymentByProject(projectId);
    
    }
    @GetMapping("/api/projects/{projectId}/payment-summary")
    public Map<String, Object> getProjectPaymentSummary(@PathVariable Long projectId) {
        return paymentService.getProjectPaymentSummary(projectId);
    }
    

    
}
