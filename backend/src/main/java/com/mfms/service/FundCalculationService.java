package com.mfms.service;

import com.mfms.dto.*;
import com.mfms.entity.*;
import com.mfms.enums.*;
import com.mfms.exception.BusinessException;
import com.mfms.exception.ResourceNotFoundException;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.mfms.util.ContributionSchedule;

@Service
@RequiredArgsConstructor
public class FundCalculationService {

    private final ContributionRepository contributionRepository;
    private final InterestPaymentRepository interestPaymentRepository;
    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final ExternalBorrowerRepository borrowerRepository;
    private final EntityMapper mapper;

    public BigDecimal getTotalContributions() {
        return memberRepository.sumActiveDeposits();
    }

    public BigDecimal getTotalInterestCollected() {
        return memberRepository.sumImportedInterestPaid()
                .add(interestPaymentRepository.sumInterestForNonImportedLoans());
    }

    public BigDecimal getOutstandingPrincipal() {
        return memberRepository.sumImportedCurrentLoans()
                .add(loanRepository.sumOutstandingForNonImportedLoans());
    }

    public BigDecimal getAvailableFund() {
        BigDecimal contributions = getTotalContributions();
        BigDecimal interest = getTotalInterestCollected();
        BigDecimal outstanding = getOutstandingPrincipal();
        return contributions.add(interest).subtract(outstanding);
    }

    public BigDecimal calculatePendingInterest(Loan loan) {
        if (loan.isImportedBalance() || loan.getStatus() != LoanStatus.ACTIVE
                || loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        LocalDate lastDate = interestPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loan.getId()).stream()
                .map(InterestPayment::getPaymentDate)
                .max(LocalDate::compareTo)
                .orElse(loan.getLoanDate());

        long months = ChronoUnit.MONTHS.between(
                lastDate.withDayOfMonth(1),
                LocalDate.now().withDayOfMonth(1)
        );
        if (months <= 0) return BigDecimal.ZERO;

        BigDecimal monthlyInterest = mapper.calculateMonthlyInterest(
                loan.getOutstandingAmount(), loan.getInterestRate());
        return monthlyInterest.multiply(BigDecimal.valueOf(months)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPendingInterest() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .map(this::calculatePendingInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public DashboardResponse buildAdminDashboard() {
        BigDecimal totalContributions = getTotalContributions();
        BigDecimal totalInterest = getTotalInterestCollected();
        BigDecimal outstanding = getOutstandingPrincipal();
        BigDecimal availableFund = totalContributions.add(totalInterest).subtract(outstanding);
        long totalMembers = memberRepository.countByStatus(EntityStatus.ACTIVE);
        BigDecimal totalExpectedCollection = ContributionSchedule.expectedAmountForOneMember()
                .multiply(BigDecimal.valueOf(totalMembers));
        BigDecimal totalPendingCollection = totalExpectedCollection
                .subtract(totalContributions)
                .max(BigDecimal.ZERO);

        List<ChartDataPoint> contributionTrend = new ArrayList<>();
        for (Object[] row : contributionRepository.monthlyContributionTrend()) {
            contributionTrend.add(ChartDataPoint.builder()
                    .label(row[1] + "/" + row[0])
                    .value((BigDecimal) row[2])
                    .build());
        }

        List<ChartDataPoint> interestTrend = new ArrayList<>();
        for (Object[] row : interestPaymentRepository.monthlyInterestTrend()) {
            interestTrend.add(ChartDataPoint.builder()
                    .label(row[1] + "/" + row[0])
                    .value((BigDecimal) row[2])
                    .build());
        }

        List<ChartDataPoint> loanDistribution = new ArrayList<>();
        for (Object[] row : loanRepository.loanDistribution()) {
            loanDistribution.add(ChartDataPoint.builder()
                    .category(row[0].toString())
                    .value(BigDecimal.valueOf((Long) row[1]))
                    .build());
        }

        return DashboardResponse.builder()
                .totalMembers(totalMembers)
                .totalContributions(totalContributions)
                .totalExpectedCollection(totalExpectedCollection)
                .totalActualCollection(totalContributions)
                .totalPendingCollection(totalPendingCollection)
                .expectedContributionMonths(ContributionSchedule.expectedMonths())
                .totalFundCollected(totalContributions.add(totalInterest))
                .totalInterestEarned(totalInterest)
                .availableFund(availableFund)
                .moneyLoanedOut(outstanding)
                .outstandingPrincipal(outstanding)
                .activeLoans(loanRepository.countByStatus(LoanStatus.ACTIVE))
                .closedLoans(loanRepository.countByStatus(LoanStatus.CLOSED))
                .externalBorrowers(borrowerRepository.countByStatus(EntityStatus.ACTIVE))
                .pendingContributions(totalPendingCollection.divide(
                        ContributionSchedule.MONTHLY_AMOUNT, 0, RoundingMode.DOWN).longValue())
                .totalPendingContributionAmount(totalPendingCollection)
                .pendingInterest(getTotalPendingInterest())
                .contributionTrend(contributionTrend)
                .interestTrend(interestTrend)
                .loanDistribution(loanDistribution)
                .build();
    }
}
