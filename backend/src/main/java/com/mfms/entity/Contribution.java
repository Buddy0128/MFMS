package com.mfms.entity;

import com.mfms.enums.ContributionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contributions", indexes = {
        @Index(name = "idx_contribution_status", columnList = "status"),
        @Index(name = "idx_contribution_date", columnList = "contrib_year, contrib_month")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_member_month_year", columnNames = {"member_id", "contrib_month", "contrib_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "contrib_month", nullable = false)
    private Integer month;

    @Column(name = "contrib_year", nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amount = new BigDecimal("1000.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContributionStatus status = ContributionStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
