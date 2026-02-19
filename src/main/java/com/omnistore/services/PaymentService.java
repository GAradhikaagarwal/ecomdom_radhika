package com.omnistore.services;

import com.omnistore.entity.Order;
import com.omnistore.entity.Payment;
import com.omnistore.exception.BadRequestException;
import com.omnistore.exception.ResourceNotFoundException;
import com.omnistore.repository.OrderRepository;
import com.omnistore.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    // Existing mock payment method (can be removed or kept for other payment types)
    public Payment pay(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(order.getTotalAmount());
        payment.setProvider("MOCK_GATEWAY");
        payment.setStatus("INITIATED");
        payment.setCreatedAt(LocalDateTime.now());

        // mock response
        boolean success = new Random().nextInt(10) < 9; // 90% success
        payment.setStatus(success ? "SUCCESS" : "FAILED");

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment createStripePaymentIntent(Long orderId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Check if a PaymentIntent already exists for this order
        // This prevents creating multiple payment intents for the same order
        Payment existingPayment = paymentRepository.findByOrderIdAndProvider(orderId, "STRIPE")
                .orElse(null);

        if (existingPayment != null && existingPayment.getStripePaymentIntentId() != null) {
            // Retrieve existing PaymentIntent to get the latest client secret
            PaymentIntent paymentIntent = PaymentIntent.retrieve(existingPayment.getStripePaymentIntentId());
            existingPayment.setStripeClientSecret(paymentIntent.getClientSecret());
            return paymentRepository.save(existingPayment);
        }

        // Stripe requires amount in cents
        long amountInCents = (long) (order.getTotalAmount() * 100);

        PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(amountInCents)
                .putMetadata("order_id", orderId.toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(createParams);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(order.getTotalAmount());
        payment.setProvider("STRIPE");
        payment.setStatus(paymentIntent.getStatus()); // e.g., requires_payment_method
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStripeClientSecret(paymentIntent.getClientSecret());
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment confirmStripePayment(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for PaymentIntent ID: " + paymentIntentId));

        // Update payment status based on Stripe's PaymentIntent status
        payment.setStatus(paymentIntent.getStatus());
        paymentRepository.save(payment);

        // If payment succeeded, update order status
        if ("succeeded".equals(paymentIntent.getStatus())) {
            orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                order.setStatus(com.omnistore.entity.OrderStatus.PAID);
                orderRepository.save(order);
            });
        } else if ("requires_action".equals(paymentIntent.getStatus()) || "requires_confirmation".equals(paymentIntent.getStatus())) {
            // Handle cases where further action is needed from the customer
            // This might involve redirecting the customer to a 3D Secure page, etc.
            // For now, we'll just update the status.
            payment.setStatus("REQUIRES_ACTION");
            paymentRepository.save(payment);
        } else if ("canceled".equals(paymentIntent.getStatus())) {
            payment.setStatus("FAILED"); // Or a more specific "CANCELED" status
            paymentRepository.save(payment);
        }

        return payment;
    }

    // Method to handle webhook events (to be implemented later)
    public void handleStripeWebhook(String payload, String sigHeader) {
        // This will be more complex, involving signature verification and event parsing
        // For now, it's a placeholder
        System.out.println("Received Stripe webhook event.");
    }
}
