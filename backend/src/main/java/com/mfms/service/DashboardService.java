package com.mfms.service;

import com.mfms.dto.*;
import com.mfms.enums.BorrowerType;
import com.mfms.enums.LoanStatus;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.InterestPaymentRepository;
import com.mfms.repository.LoanRepository;
import com.mfms.repository.PrincipalPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FundCalculationService fundCalculationService;
    private final MemberService memberService;
    private final ContributionService contributionService;
    private final LoanService loanService;
    private final ActivityLogService activityLogService;
    private final LoanRepository loanRepository;
    private final InterestPaymentRepository interestPaymentRepository;
    private final PrincipalPaymentRepository principalPaymentRepository;
    private final EntityMapper mapper;

    public DashboardResponse getAdminDashboard() {
        DashboardResponse dashboard = fundCalculationService.buildAdminDashboard();
        dashboard.setRecentContributions(contributionService.getAll().stream().limit(5).toList());
        dashboard.setRecentLoans(loanService.getAllLoans().stream().limit(5).toList());
        dashboard.setRecentInterestPayments(
                interestPaymentRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .limit(5)
                        .map(mapper::toInterestPaymentResponse)
                        .toList()
        );
        dashboard.setRecentActivities(activityLogService.getRecentLogs().stream().limit(10).toList());
        return dashboard;
    }

    public MemberDashboardResponse getMemberDashboard(Long memberId) {
        MemberResponse member = memberService.getMember(memberId);
        List<LoanResponse> loans = loanService.getLoansByBorrower(BorrowerType.MEMBER, memberId);
        List<ContributionResponse> contributions = contributionService.getByMemberId(memberId);

        BigDecimal totalContributions = member.isImportedData()
                ? member.getTotalDeposit()
                : contributions.stream()
                    .filter(c -> c.getStatus() == com.mfms.enums.ContributionStatus.PAID)
                    .map(ContributionResponse::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstanding = member.isImportedData()
                ? member.getCurrentLoanAmount()
                : loans.stream()
                .filter(l -> l.getStatus() == LoanStatus.ACTIVE)
                .map(LoanResponse::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingInterest = loans.stream()
                .map(LoanResponse::getPendingInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterestPaid = member.isImportedData()
                ? member.getTotalInterestPaid()
                : loans.stream()
                .map(LoanResponse::getTotalInterestPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentResponse> recentTransactions = new ArrayList<>();
        loans.forEach(l -> {
            recentTransactions.addAll(loanService.getPrincipalPayments(l.getId()));
            recentTransactions.addAll(loanService.getInterestPayments(l.getId()));
        });
        recentTransactions.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return MemberDashboardResponse.builder()
                .member(member)
                .contributionSummary(contributionService.getSummary(memberId))
                .totalContributions(totalContributions)
                .outstandingPrincipal(outstanding)
                .pendingInterest(pendingInterest)
                .totalInterestPaid(totalInterestPaid)
                .availableFund(fundCalculationService.getAvailableFund())
                .contributions(contributions)
                .loans(loans)
                .recentTransactions(recentTransactions.stream().limit(10).toList())
                .build();
    }
}
