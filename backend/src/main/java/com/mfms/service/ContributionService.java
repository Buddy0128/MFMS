package com.mfms.service;

import com.mfms.dto.ContributionRequest;
import com.mfms.dto.ContributionResponse;
import com.mfms.dto.ContributionSummaryResponse;
import com.mfms.dto.PendingContributionReportResponse;
import com.mfms.entity.Contribution;
import com.mfms.entity.Member;
import com.mfms.enums.ContributionStatus;
import com.mfms.enums.EntityStatus;
import com.mfms.exception.BusinessException;
import com.mfms.exception.ResourceNotFoundException;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.ContributionRepository;
import com.mfms.repository.MemberRepository;
import com.mfms.util.ContributionSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final EntityMapper mapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public List<ContributionResponse> getAll() {
        syncAllTimelines();
        return contributionRepository.findAll().stream()
                .sorted((a, b) -> {
                    int yearCmp = b.getYear().compareTo(a.getYear());
                    return yearCmp != 0 ? yearCmp : b.getMonth().compareTo(a.getMonth());
                })
                .map(mapper::toContributionResponse)
                .toList();
    }

    @Transactional
    public List<ContributionResponse> getByMemberId(Long memberId) {
        syncTimeline(memberId);
        return contributionRepository.findByMemberIdOrderByYearDescMonthDesc(memberId).stream()
                .map(mapper::toContributionResponse)
                .toList();
    }

    @Transactional
    public List<ContributionResponse> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        syncAllTimelines();
        return contributionRepository.search(query).stream()
                .map(mapper::toContributionResponse)
                .toList();
    }

    @Transactional
    public List<ContributionResponse> filter(ContributionStatus status, Integer year, Integer month) {
        syncAllTimelines();
        return contributionRepository.findAll().stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> year == null || c.getYear().equals(year))
                .filter(c -> month == null || c.getMonth().equals(month))
                .map(mapper::toContributionResponse)
                .toList();
    }

    @Transactional
    public List<ContributionResponse> getMonthlyReport(int year, int month) {
        syncAllTimelines();
        return contributionRepository.findByYearAndMonth(year, month).stream()
                .map(mapper::toContributionResponse)
                .toList();
    }

    @Transactional
    public ContributionResponse recordContribution(ContributionRequest request, Long adminId) {
        throw new BusinessException(
                "Manual monthly contribution entry is disabled. Update the member Total Deposit amount instead.");
    }

    @Transactional
    public ContributionResponse updateStatus(Long id, ContributionStatus status, Long adminId) {
        Contribution contribution = contributionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution not found"));
        if (contribution.getStatus() == status) {
            return mapper.toContributionResponse(contribution);
        }

        Member member = contribution.getMember();
        BigDecimal totalDeposit = member.getTotalDeposit() != null ? member.getTotalDeposit() : BigDecimal.ZERO;
        if (status == ContributionStatus.PAID) {
            member.setTotalDeposit(totalDeposit.add(ContributionSchedule.MONTHLY_AMOUNT));
        } else {
            BigDecimal updatedDeposit = totalDeposit.subtract(ContributionSchedule.MONTHLY_AMOUNT);
            if (updatedDeposit.signum() < 0) {
                throw new BusinessException("Total Deposit cannot be negative");
            }
            member.setTotalDeposit(updatedDeposit);
        }

        memberRepository.save(member);
        rebuildTimeline(member);
        activityLogService.log(adminId, "Updated Contribution Status",
                member.getFullName() + " marked " + ContributionSchedule.format(YearMonth.of(
                        contribution.getYear(), contribution.getMonth())) + " as " + status);

        return contributionRepository
                .findByMemberIdAndMonthAndYear(member.getId(), contribution.getMonth(), contribution.getYear())
                .map(mapper::toContributionResponse)
                .orElseGet(() -> mapper.toContributionResponse(contribution));
    }

    public ContributionSummaryResponse getSummary(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        return toSummary(member);
    }

    public List<ContributionSummaryResponse> getAllSummaries() {
        return memberRepository.findByStatus(EntityStatus.ACTIVE).stream()
                .map(this::toSummary)
                .toList();
    }

    public PendingContributionReportResponse getPendingReport() {
        List<ContributionSummaryResponse> summaries = getAllSummaries();
        BigDecimal actual = summaries.stream()
                .map(ContributionSummaryResponse::getTotalDeposit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expected = ContributionSchedule.expectedAmountForOneMember()
                .multiply(BigDecimal.valueOf(summaries.size()));
        return PendingContributionReportResponse.builder()
                .expectedMonths(ContributionSchedule.expectedMonths())
                .monthlyContribution(ContributionSchedule.MONTHLY_AMOUNT)
                .totalMembers(summaries.size())
                .totalExpectedCollection(expected)
                .totalActualCollection(actual)
                .totalPendingCollection(expected.subtract(actual).max(BigDecimal.ZERO))
                .members(summaries.stream()
                        .filter(summary -> summary.getPendingMonths() > 0 || summary.getExtraAmount().signum() > 0)
                        .toList())
                .build();
    }

    @Transactional
    public void rebuildTimeline(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        rebuildTimeline(member);
    }

    @Transactional
    public void rebuildTimeline(Member member) {
        if (member.getTotalDeposit() == null || member.getTotalDeposit().signum() < 0) {
            throw new BusinessException("Total Deposit cannot be negative");
        }

        syncTimeline(member);
    }

    @Transactional
    public void syncTimeline(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        syncTimeline(member);
    }

    @Transactional
    public void syncTimeline(Member member) {
        if (member.getTotalDeposit() == null || member.getTotalDeposit().signum() < 0) {
            throw new BusinessException("Total Deposit cannot be negative");
        }

        int paidMonths = ContributionSchedule.paidMonths(member.getTotalDeposit());
        List<YearMonth> periods = ContributionSchedule.expectedPeriods();
        for (int index = 0; index < periods.size(); index++) {
            YearMonth period = periods.get(index);
            boolean paid = index < paidMonths;
            Contribution contribution = contributionRepository
                    .findByMemberIdAndMonthAndYear(member.getId(), period.getMonthValue(), period.getYear())
                    .orElseGet(() -> Contribution.builder()
                            .member(member)
                            .month(period.getMonthValue())
                            .year(period.getYear())
                            .build());
            contribution.setAmount(ContributionSchedule.MONTHLY_AMOUNT);
            contribution.setStatus(paid ? ContributionStatus.PAID : ContributionStatus.PENDING);
            contribution.setPaymentDate(paid ? period.atEndOfMonth() : null);
            contributionRepository.save(contribution);
        }

        if (!periods.isEmpty()) {
            YearMonth lastExpected = periods.get(periods.size() - 1);
            contributionRepository.deleteFutureForMember(
                    member.getId(), lastExpected.getYear(), lastExpected.getMonthValue());
        }
    }

    @Transactional
    public void rebuildAllTimelines() {
        memberRepository.findByStatus(EntityStatus.ACTIVE).forEach(this::rebuildTimeline);
    }

    @Transactional
    public void syncAllTimelines() {
        memberRepository.findByStatus(EntityStatus.ACTIVE).forEach(this::syncTimeline);
    }

    private ContributionSummaryResponse toSummary(Member member) {
        BigDecimal totalDeposit = member.getTotalDeposit() != null ? member.getTotalDeposit() : BigDecimal.ZERO;
        if (totalDeposit.signum() < 0) {
            throw new BusinessException("Total Deposit cannot be negative");
        }
        int expectedMonths = ContributionSchedule.expectedMonths();
        int paidMonths = ContributionSchedule.paidMonths(totalDeposit);
        int pendingMonths = ContributionSchedule.pendingMonths(totalDeposit);
        BigDecimal expectedAmount = ContributionSchedule.expectedAmountForOneMember();
        BigDecimal pendingAmount = ContributionSchedule.MONTHLY_AMOUNT
                .multiply(BigDecimal.valueOf(pendingMonths));
        BigDecimal extraAmount = ContributionSchedule.extraAmount(totalDeposit);
        return ContributionSummaryResponse.builder()
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .memberName(member.getFullName())
                .totalDeposit(totalDeposit)
                .monthlyContribution(ContributionSchedule.MONTHLY_AMOUNT)
                .expectedMonths(expectedMonths)
                .paidMonths(paidMonths)
                .pendingMonths(pendingMonths)
                .lastPaidMonth(ContributionSchedule.lastPaidMonth(totalDeposit))
                .completionPercentage(ContributionSchedule.completionPercentage(totalDeposit))
                .expectedAmount(expectedAmount)
                .pendingAmount(pendingAmount)
                .extraAmount(extraAmount)
                .validationMessage(extraAmount.signum() > 0 ? "Extra amount detected: ₹" + extraAmount : null)
                .build();
    }
}
