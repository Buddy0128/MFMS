package com.mfms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalMembers;
    private BigDecimal totalContributions;
    private BigDecimal totalExpectedCollection;
    private BigDecimal totalActualCollection;
    private BigDecimal totalPendingCollection;
    private int expectedContributionMonths;
    private BigDecimal totalFundCollected;
    private BigDecimal totalInterestEarned;
    private BigDecimal availableFund;
    private BigDecimal moneyLoanedOut;
    private BigDecimal outstandingPrincipal;
    private long activeLoans;
    private long closedLoans;
    private long externalBorrowers;
    private long pendingContributions;
    private BigDecimal totalPendingContributionAmount;
    private BigDecimal pendingInterest;
    private List<ChartDataPoint> contributionTrend;
    private List<ChartDataPoint> interestTrend;
    private List<ChartDataPoint> loanDistribution;
    private List<ContributionResponse> recentContributions;
    private List<LoanResponse> recentLoans;
    private List<PaymentResponse> recentInterestPayments;
    private List<ActivityLogResponse> recentActivities;
}
