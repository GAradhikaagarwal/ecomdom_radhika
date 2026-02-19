package com.omnistore.repository;

import com.omnistore.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderIdAndProvider(Long orderId, String provider);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
