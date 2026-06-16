package com.mfms.dto;

import com.mfms.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String memberCode;
    private String fullName;
    private String phoneNumber;
    private LocalDate joinDate;
    private EntityStatus status;
    private BigDecimal totalDeposit;
    private BigDecimal currentLoanAmount;
    private BigDecimal totalInterestPaid;
    private int paidMonths;
    private int pendingMonths;
    private String lastPaidMonth;
    private BigDecimal completionPercentage;
    private BigDecimal extraAmount;
    private String contributionValidationMessage;
    private boolean importedData;
    private LocalDateTime createdAt;
}
