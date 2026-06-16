package com.mfms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionSummaryResponse {
    private Long memberId;
    private String memberCode;
    private String memberName;
    private BigDecimal totalDeposit;
    private BigDecimal monthlyContribution;
    private int expectedMonths;
    private int paidMonths;
    private int pendingMonths;
    private String lastPaidMonth;
    private BigDecimal completionPercentage;
    private BigDecimal expectedAmount;
    private BigDecimal pendingAmount;
    private BigDecimal extraAmount;
    private String validationMessage;
}
