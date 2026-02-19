package com.omnistore.controller;

import com.omnistore.entity.Payment;
import com.omnistore.services.OrderService;
import com.omnistore.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.ApiResource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService,
                             OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<Payment> pay(@PathVariable Long orderId) {
        Payment payment = paymentService.pay(orderId);
        orderService.updateStatusAfterPayment(orderId, "SUCCESS".equals(payment.getStatus()));
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/stripe/create-payment-intent/{orderId}")
    public ResponseEntity<Payment> createStripePaymentIntent(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.createStripePaymentIntent(orderId));
        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/stripe/confirm/{paymentIntentId}")
    public ResponseEntity<Payment> confirmStripePayment(@PathVariable String paymentIntentId) {
        try {
            return ResponseEntity.ok(paymentService.confirmStripePayment(paymentIntentId));
        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) throws IOException {
        String payload = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        Event event;
        try {
            event = ApiResource.GSON.fromJson(payload, Event.class);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Invalid payload");
        }

        // Handle the event
        if ("payment_intent.succeeded".equals(event.getType())) {
            System.out.println("Payment succeeded: " + event.getId());
            // You can add logic here to update the order status if needed
        }

        return ResponseEntity.ok("Received");
    }
}
