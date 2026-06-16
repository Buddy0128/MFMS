package com.mfms.repository;

import com.mfms.entity.InterestPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface InterestPaymentRepository extends JpaRepository<InterestPayment, Long> {
    List<InterestPayment> findByLoanIdOrderByPaymentDateDesc(Long loanId);
    List<InterestPayment> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InterestPayment i")
    BigDecimal sumTotalInterestCollected();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM InterestPayment i " +
           "WHERE i.loan.importedBalance = false")
    BigDecimal sumInterestForNonImportedLoans();

    @Query("SELECT YEAR(i.paymentDate), MONTH(i.paymentDate), SUM(i.amount) FROM InterestPayment i " +
           "GROUP BY YEAR(i.paymentDate), MONTH(i.paymentDate) ORDER BY YEAR(i.paymentDate), MONTH(i.paymentDate)")
    List<Object[]> monthlyInterestTrend();

    void deleteByLoanId(Long loanId);
}
