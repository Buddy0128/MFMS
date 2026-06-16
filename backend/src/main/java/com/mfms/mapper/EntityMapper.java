package com.mfms.mapper;

import com.mfms.dto.*;
import com.mfms.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.mfms.util.ContributionSchedule;

@Component
public class EntityMapper {

    public MemberResponse toMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .fullName(member.getFullName())
                .phoneNumber(member.getPhoneNumber())
                .joinDate(member.getJoinDate())
                .status(member.getStatus())
                .totalDeposit(member.getTotalDeposit())
                .currentLoanAmount(member.getCurrentLoanAmount())
                .totalInterestPaid(member.getTotalInterestPaid())
                .paidMonths(ContributionSchedule.paidMonths(member.getTotalDeposit()))
                .pendingMonths(ContributionSchedule.pendingMonths(member.getTotalDeposit()))
                .lastPaidMonth(ContributionSchedule.lastPaidMonth(member.getTotalDeposit()))
                .completionPercentage(ContributionSchedule.completionPercentage(member.getTotalDeposit()))
                .extraAmount(ContributionSchedule.extraAmount(member.getTotalDeposit()))
                .contributionValidationMessage(
                        ContributionSchedule.extraAmount(member.getTotalDeposit()).signum() > 0
                                ? "Extra amount detected: ₹" + ContributionSchedule.extraAmount(member.getTotalDeposit())
                                : null)
                .importedData(member.isImportedData())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public ExternalBorrowerResponse toBorrowerResponse(ExternalBorrower borrower) {
        return ExternalBorrowerResponse.builder()
                .id(borrower.getId())
                .fullName(borrower.getFullName())
                .phoneNumber(borrower.getPhoneNumber())
                .address(borrower.getAddress())
                .status(borrower.getStatus())
                .createdAt(borrower.getCreatedAt())
                .build();
    }

    public ContributionResponse toContributionResponse(Contribution c) {
        return ContributionResponse.builder()
                .id(c.getId())
                .memberId(c.getMember().getId())
                .memberName(c.getMember().getFullName())
                .memberCode(c.getMember().getMemberCode())
                .month(c.getMonth())
                .year(c.getYear())
                .amount(c.getAmount())
                .status(c.getStatus())
                .paymentDate(c.getPaymentDate())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public LoanResponse toLoanResponse(Loan loan, String borrowerName,
                                       BigDecimal pendingInterest, BigDecimal totalInterestPaid) {
        BigDecimal monthlyInterest = calculateMonthlyInterest(loan.getOutstandingAmount(), loan.getInterestRate());
        return LoanResponse.builder()
                .id(loan.getId())
                .borrowerType(loan.getBorrowerType())
                .borrowerId(loan.getBorrowerId())
                .borrowerName(borrowerName)
                .loanAmount(loan.getLoanAmount())
                .outstandingAmount(loan.getOutstandingAmount())
                .interestRate(loan.getInterestRate())
                .monthlyInterest(monthlyInterest)
                .pendingInterest(pendingInterest)
                .totalInterestPaid(totalInterestPaid)
                .loanDate(loan.getLoanDate())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .build();
    }

    public PaymentResponse toPrincipalPaymentResponse(PrincipalPayment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .loanId(p.getLoan().getId())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public PaymentResponse toInterestPaymentResponse(InterestPayment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .loanId(p.getLoan().getId())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .createdAt(p.getCreatedAt())
                .build();
    }

    public ActivityLogResponse toActivityLogResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .adminName(log.getAdmin() != null ? log.getAdmin().getName() : "System")
                .action(log.getAction())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }

    public BigDecimal calculateMonthlyInterest(BigDecimal outstanding, BigDecimal rate) {
        if (outstanding == null || rate == null || outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return outstanding.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
