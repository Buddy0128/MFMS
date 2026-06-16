package com.mfms.repository;

import com.mfms.entity.Contribution;
import com.mfms.enums.ContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByMemberIdOrderByYearDescMonthDesc(Long memberId);
    long countByStatus(ContributionStatus status);
    List<Contribution> findByStatus(ContributionStatus status);

    Optional<Contribution> findByMemberIdAndMonthAndYear(Long memberId, Integer month, Integer year);
    void deleteByMemberId(Long memberId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c WHERE c.status = 'PAID'")
    BigDecimal sumPaidContributions();

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c " +
           "WHERE c.status = 'PAID' AND c.member.importedData = false")
    BigDecimal sumPaidContributionsForNonImportedMembers();

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Contribution c WHERE c.status = 'PENDING'")
    BigDecimal sumPendingContributionAmount();

    @Query("SELECT c FROM Contribution c JOIN c.member m WHERE " +
           "LOWER(m.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "m.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<Contribution> search(@Param("query") String query);

    @Query("SELECT c.year, c.month, SUM(c.amount) FROM Contribution c " +
           "WHERE c.status = 'PAID' GROUP BY c.year, c.month ORDER BY c.year, c.month")
    List<Object[]> monthlyContributionTrend();

    @Query("SELECT c FROM Contribution c WHERE c.year = :year AND c.month = :month")
    List<Contribution> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Modifying
    @Query("DELETE FROM Contribution c WHERE c.year > :year OR (c.year = :year AND c.month > :month)")
    void deleteAfter(@Param("year") int year, @Param("month") int month);

    @Modifying
    @Query("DELETE FROM Contribution c WHERE c.member.id = :memberId AND (c.year > :year OR (c.year = :year AND c.month > :month))")
    void deleteFutureForMember(@Param("memberId") Long memberId, @Param("year") int year, @Param("month") int month);
}
