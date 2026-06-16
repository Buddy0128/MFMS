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
public class MemberDetailResponse {
    private MemberResponse member;
    private ContributionSummaryResponse contributionSummary;
    private BigDecimal outstandingAmount;
    private BigDecimal pendingInterest;
    private BigDecimal totalInterestPaid;
    private List<ContributionResponse> contributions;
    private List<LoanResponse> loans;
    private List<PaymentResponse> interestPayments;
}
