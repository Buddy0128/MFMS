package com.mfms.repository;

import com.mfms.entity.PrincipalPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrincipalPaymentRepository extends JpaRepository<PrincipalPayment, Long> {
    List<PrincipalPayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
    List<PrincipalPayment> findTop10ByOrderByCreatedAtDesc();
    void deleteByLoanId(Long loanId);
}
