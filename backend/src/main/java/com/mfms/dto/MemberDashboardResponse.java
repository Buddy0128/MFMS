package com.mfms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDashboardResponse {
    private MemberResponse member;
    private ContributionSummaryResponse contributionSummary;
    private BigDecimal totalContributions;
    private BigDecimal outstandingPrincipal;
    private BigDecimal pendingInterest;
    private BigDecimal totalInterestPaid;
    private BigDecimal availableFund;
    private List<ContributionResponse> contributions;
    private List<LoanResponse> loans;
    private List<PaymentResponse> recentTransactions;
}
