package com.mfms.repository;

import com.mfms.entity.Loan;
import com.mfms.enums.BorrowerType;
import com.mfms.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByBorrowerTypeAndBorrowerIdOrderByLoanDateDesc(BorrowerType type, Long borrowerId);
    long countByStatus(LoanStatus status);
    List<Loan> findByStatus(LoanStatus status);
    Optional<Loan> findFirstByBorrowerTypeAndBorrowerIdAndStatusAndImportedBalanceOrderByLoanDateAscCreatedAtAsc(
            BorrowerType type, Long borrowerId, LoanStatus status, boolean importedBalance);

    @Query("SELECT COALESCE(SUM(l.outstandingAmount), 0) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal sumOutstandingPrincipal();

    @Query("SELECT COALESCE(SUM(l.outstandingAmount), 0) FROM Loan l " +
           "WHERE l.status = 'ACTIVE' AND l.importedBalance = false")
    BigDecimal sumOutstandingForNonImportedLoans();

    @Query("SELECT COALESCE(SUM(l.loanAmount), 0) FROM Loan l")
    BigDecimal sumTotalLoaned();

    @Query("SELECT l FROM Loan l WHERE CAST(l.id AS string) LIKE CONCAT('%', :query, '%')")
    List<Loan> searchById(@Param("query") String query);

    List<Loan> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT l.borrowerType, COUNT(l) FROM Loan l GROUP BY l.borrowerType")
    List<Object[]> loanDistribution();

    Optional<Loan> findByBorrowerTypeAndBorrowerIdAndImportedBalance(
            BorrowerType type, Long borrowerId, boolean importedBalance);
}
