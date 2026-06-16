package com.mfms.repository;

import com.mfms.entity.Member;
import com.mfms.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByPhoneNumberAndStatus(String phoneNumber, EntityStatus status);
    Optional<Member> findByPhoneNumber(String phoneNumber);
    Optional<Member> findByMemberCode(String memberCode);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByMemberCode(String memberCode);
    long countByStatus(EntityStatus status);

    @Query("SELECT COALESCE(SUM(m.totalDeposit), 0) FROM Member m WHERE m.importedData = true")
    java.math.BigDecimal sumImportedDeposits();

    @Query("SELECT COALESCE(SUM(m.totalDeposit), 0) FROM Member m WHERE m.status = 'ACTIVE'")
    java.math.BigDecimal sumActiveDeposits();

    @Query("SELECT COALESCE(SUM(m.currentLoanAmount), 0) FROM Member m WHERE m.importedData = true")
    java.math.BigDecimal sumImportedCurrentLoans();

    @Query("SELECT COALESCE(SUM(m.totalInterestPaid), 0) FROM Member m WHERE m.importedData = true")
    java.math.BigDecimal sumImportedInterestPaid();

    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "m.phoneNumber LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(m.memberCode) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Member> search(@Param("query") String query);

    List<Member> findByStatus(EntityStatus status);
}
