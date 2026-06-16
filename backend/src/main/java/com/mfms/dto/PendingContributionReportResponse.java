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
public class PendingContributionReportResponse {
    private int expectedMonths;
    private BigDecimal monthlyContribution;
    private long totalMembers;
    private BigDecimal totalExpectedCollection;
    private BigDecimal totalActualCollection;
    private BigDecimal totalPendingCollection;
    private List<ContributionSummaryResponse> members;
}
