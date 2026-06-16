package com.mfms.entity;

import com.mfms.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_phone", columnList = "phone_number"),
        @Index(name = "idx_member_code", columnList = "member_code"),
        @Index(name = "idx_member_status", columnList = "status"),
        @Index(name = "idx_member_name", columnList = "full_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_code", nullable = false, unique = true, length = 20)
    private String memberCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @Column(name = "total_deposit", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalDeposit = BigDecimal.ZERO;

    @Column(name = "current_loan_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal currentLoanAmount = BigDecimal.ZERO;

    @Column(name = "total_interest_paid", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalInterestPaid = BigDecimal.ZERO;

    @Column(name = "imported_data", nullable = false)
    @Builder.Default
    private boolean importedData = false;

    @Column(name = "last_imported_at")
    private LocalDateTime lastImportedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
