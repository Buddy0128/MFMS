package com.mfms.service;

import com.mfms.dto.*;
import com.mfms.entity.*;
import com.mfms.enums.*;
import com.mfms.exception.BusinessException;
import com.mfms.exception.ResourceNotFoundException;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final BigDecimal MEMBER_RATE = new BigDecimal("1.00");
    private static final BigDecimal EXTERNAL_RATE = new BigDecimal("5.00");

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final ExternalBorrowerRepository borrowerRepository;
    private final PrincipalPaymentRepository principalPaymentRepository;
    private final InterestPaymentRepository interestPaymentRepository;
    private final EntityMapper mapper;
    private final FundCalculationService fundCalculationService;
    private final ActivityLogService activityLogService;

    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toLoanResponse)
                .toList();
    }

    public List<LoanResponse> getLoansByBorrower(BorrowerType type, Long borrowerId) {
        List<LoanResponse> responses = loanRepository.findByBorrowerTypeAndBorrowerIdOrderByLoanDateDesc(type, borrowerId).stream()
                .map(this::toLoanResponse)
                .toList();
        return mergeActiveMemberLoanResponses(responses);
    }

    public List<LoanResponse> search(String query, LoanStatus status) {
        List<Loan> loans = loanRepository.findAll();
        List<LoanResponse> responses = loans.stream()
                .filter(l -> status == null || l.getStatus() == status)
                .filter(l -> query == null || query.isBlank()
                        || l.getId().toString().contains(query)
                        || getBorrowerName(l).toLowerCase().contains(query.toLowerCase()))
                .map(this::toLoanResponse)
                .toList();
        return mergeActiveMemberLoanResponses(responses);
    }

    public LoanResponse getLoan(Long id) {
        return toLoanResponse(findLoan(id));
    }

    @Transactional
    public LoanResponse updateLoan(Long id, LoanUpdateRequest request, Long adminId) {
        Loan loan = findLoan(id);
        BigDecimal paidPrincipal = loan.getLoanAmount().subtract(loan.getOutstandingAmount());
        BigDecimal newOutstanding = request.getLoanAmount().subtract(paidPrincipal);
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Loan amount cannot be less than principal already paid");
        }

        BigDecimal previousAmount = loan.getLoanAmount();
        BigDecimal previousOutstanding = loan.getOutstandingAmount();
        loan.setLoanAmount(request.getLoanAmount());
        loan.setOutstandingAmount(newOutstanding);
        if (request.getLoanDate() != null) {
            loan.setLoanDate(request.getLoanDate());
        }
        loan.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) > 0 ? LoanStatus.ACTIVE : LoanStatus.CLOSED);
        loan = loanRepository.save(loan);
        syncImportedMemberLoanBalance(loan);

        activityLogService.log(adminId, "Updated Loan",
                "Loan ID: " + loan.getId()
                        + ", Previous Amount: ₹" + previousAmount
                        + ", New Amount: ₹" + loan.getLoanAmount()
                        + ", Previous Outstanding: ₹" + previousOutstanding
                        + ", New Outstanding: ₹" + loan.getOutstandingAmount());
        return toLoanResponse(loan);
    }

    @Transactional
    public LoanResponse issueLoan(LoanRequest request, Long adminId) {
        validateBorrower(request.getBorrowerType(), request.getBorrowerId());
        BigDecimal rate = request.getBorrowerType() == BorrowerType.MEMBER ? MEMBER_RATE : EXTERNAL_RATE;

        if (request.getBorrowerType() == BorrowerType.MEMBER) {
            Loan existingLoan = findCanonicalActiveMemberLoan(request.getBorrowerId());
            if (existingLoan != null) {
                BigDecimal previousLoanAmount = existingLoan.getLoanAmount();
                BigDecimal previousOutstanding = existingLoan.getOutstandingAmount();
                existingLoan.setLoanAmount(previousLoanAmount.add(request.getLoanAmount()));
                existingLoan.setOutstandingAmount(previousOutstanding.add(request.getLoanAmount()));
                existingLoan = loanRepository.save(existingLoan);
                syncImportedMemberLoanBalance(existingLoan);

                String borrowerName = getBorrowerName(existingLoan);
                activityLogService.log(adminId, "Added Loan Amount",
                        "Borrower: " + borrowerName
                                + ", Loan ID: " + existingLoan.getId()
                                + ", Added: ₹" + request.getLoanAmount()
                                + ", Previous Outstanding: ₹" + previousOutstanding
                                + ", New Outstanding: ₹" + existingLoan.getOutstandingAmount());
                return toLoanResponse(existingLoan);
            }
        }

        Loan loan = Loan.builder()
                .borrowerType(request.getBorrowerType())
                .borrowerId(request.getBorrowerId())
                .loanAmount(request.getLoanAmount())
                .outstandingAmount(request.getLoanAmount())
                .interestRate(rate)
                .loanDate(request.getLoanDate() != null ? request.getLoanDate() : LocalDate.now())
                .status(LoanStatus.ACTIVE)
                .build();

        loan = loanRepository.save(loan);
        String borrowerName = getBorrowerName(loan);
        activityLogService.log(adminId, "Created Loan",
                "Borrower: " + borrowerName + ", Amount: ₹" + loan.getLoanAmount());
        return toLoanResponse(loan);
    }

    @Transactional
    public LoanResponse closeLoan(Long id, Long adminId) {
        Loan loan = findLoan(id);
        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Cannot close loan with outstanding principal");
        }
        loan.setStatus(LoanStatus.CLOSED);
        loan = loanRepository.save(loan);
        activityLogService.log(adminId, "Closed Loan", "Loan ID: " + id);
        return toLoanResponse(loan);
    }

    @Transactional
    public PaymentResponse addPrincipalPayment(PaymentRequest request, Long adminId) {
        Loan loan = findLoan(request.getLoanId());
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Cannot add payment to closed loan");
        }
        if (request.getAmount().compareTo(loan.getOutstandingAmount()) > 0) {
            throw new BusinessException("Payment exceeds outstanding amount");
        }

        PrincipalPayment payment = PrincipalPayment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .build();
        payment = principalPaymentRepository.save(payment);

        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(request.getAmount()));
        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepository.save(loan);

        activityLogService.log(adminId, "Principal Payment",
                "Loan ID: " + loan.getId() + ", Amount: ₹" + request.getAmount());
        return mapper.toPrincipalPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse addInterestPayment(PaymentRequest request, Long adminId) {
        Loan loan = findLoan(request.getLoanId());
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Cannot add interest payment to closed loan");
        }

        InterestPayment payment = InterestPayment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .build();
        payment = interestPaymentRepository.save(payment);

        activityLogService.log(adminId, "Interest Payment",
                "Loan ID: " + loan.getId() + ", Amount: ₹" + request.getAmount());
        return mapper.toInterestPaymentResponse(payment);
    }

    public List<PaymentResponse> getPrincipalPayments(Long loanId) {
        return principalPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId).stream()
                .map(mapper::toPrincipalPaymentResponse)
                .toList();
    }

    public List<PaymentResponse> getInterestPayments(Long loanId) {
        return interestPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId).stream()
                .map(mapper::toInterestPaymentResponse)
                .toList();
    }

    Loan findLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
    }

    private LoanResponse toLoanResponse(Loan loan) {
        BigDecimal totalInterestPaid = interestPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loan.getId())
                .stream().map(InterestPayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingInterest = fundCalculationService.calculatePendingInterest(loan);
        return mapper.toLoanResponse(loan, getBorrowerName(loan), pendingInterest, totalInterestPaid);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void consolidateExistingActiveMemberLoans() {
        Map<Long, List<Loan>> activeLoansByMember = loanRepository.findByStatus(LoanStatus.ACTIVE).stream()
                .filter(loan -> loan.getBorrowerType() == BorrowerType.MEMBER)
                .collect(java.util.stream.Collectors.groupingBy(Loan::getBorrowerId));

        activeLoansByMember.values().stream()
                .filter(loans -> loans.size() > 1)
                .forEach(this::consolidateActiveMemberLoans);
    }

    private void consolidateActiveMemberLoans(List<Loan> activeLoans) {
        Loan canonical = activeLoans.stream()
                .min(Comparator.comparing(Loan::isImportedBalance).reversed()
                        .thenComparing(Loan::getLoanDate)
                        .thenComparing(Loan::getId))
                .orElseThrow();

        for (Loan duplicate : activeLoans) {
            if (duplicate.getId().equals(canonical.getId())) {
                continue;
            }

            canonical.setLoanAmount(canonical.getLoanAmount().add(duplicate.getLoanAmount()));
            canonical.setOutstandingAmount(canonical.getOutstandingAmount().add(duplicate.getOutstandingAmount()));

            principalPaymentRepository.findByLoanIdOrderByPaymentDateDesc(duplicate.getId()).forEach(payment -> {
                payment.setLoan(canonical);
                principalPaymentRepository.save(payment);
            });
            interestPaymentRepository.findByLoanIdOrderByPaymentDateDesc(duplicate.getId()).forEach(payment -> {
                payment.setLoan(canonical);
                interestPaymentRepository.save(payment);
            });
            principalPaymentRepository.flush();
            interestPaymentRepository.flush();
            loanRepository.delete(duplicate);
        }

        loanRepository.save(canonical);
        syncImportedMemberLoanBalance(canonical);
        activityLogService.log(null, "Merged Active Loans",
                "Member ID: " + canonical.getBorrowerId()
                        + ", Loan ID: " + canonical.getId()
                        + ", Merged Amount: ₹" + canonical.getLoanAmount());
    }

    private Loan findCanonicalActiveMemberLoan(Long memberId) {
        return loanRepository.findByBorrowerTypeAndBorrowerIdOrderByLoanDateDesc(
                        BorrowerType.MEMBER, memberId).stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE)
                .min(Comparator.comparing(Loan::isImportedBalance).reversed()
                        .thenComparing(Loan::getLoanDate)
                        .thenComparing(Loan::getId))
                .orElse(null);
    }

    private void syncImportedMemberLoanBalance(Loan loan) {
        if (!loan.isImportedBalance()) {
            return;
        }
        memberRepository.findById(loan.getBorrowerId()).ifPresent(member -> {
            member.setCurrentLoanAmount(loan.getOutstandingAmount());
            memberRepository.save(member);
        });
    }

    private List<LoanResponse> mergeActiveMemberLoanResponses(List<LoanResponse> loans) {
        Map<Long, LoanResponse> activeMemberLoans = new LinkedHashMap<>();
        return loans.stream()
                .filter(loan -> {
                    if (loan.getBorrowerType() != BorrowerType.MEMBER || loan.getStatus() != LoanStatus.ACTIVE) {
                        return true;
                    }
                    LoanResponse existing = activeMemberLoans.get(loan.getBorrowerId());
                    if (existing == null) {
                        activeMemberLoans.put(loan.getBorrowerId(), loan);
                        return true;
                    }
                    existing.setLoanAmount(existing.getLoanAmount().add(loan.getLoanAmount()));
                    existing.setOutstandingAmount(existing.getOutstandingAmount().add(loan.getOutstandingAmount()));
                    existing.setMonthlyInterest(existing.getMonthlyInterest().add(loan.getMonthlyInterest()));
                    existing.setPendingInterest(existing.getPendingInterest().add(loan.getPendingInterest()));
                    existing.setTotalInterestPaid(existing.getTotalInterestPaid().add(loan.getTotalInterestPaid()));
                    return false;
                })
                .toList();
    }

    private String getBorrowerName(Loan loan) {
        if (loan.getBorrowerType() == BorrowerType.MEMBER) {
            return memberRepository.findById(loan.getBorrowerId())
                    .map(Member::getFullName).orElse("Unknown Member");
        }
        return borrowerRepository.findById(loan.getBorrowerId())
                .map(ExternalBorrower::getFullName).orElse("Unknown Borrower");
    }

    private void validateBorrower(BorrowerType type, Long id) {
        if (type == BorrowerType.MEMBER) {
            memberRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        } else {
            borrowerRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));
        }
    }
}
