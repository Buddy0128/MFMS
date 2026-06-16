package com.mfms.service;

import com.mfms.dto.ExternalBorrowerRequest;
import com.mfms.dto.ExternalBorrowerResponse;
import com.mfms.dto.LoanResponse;
import com.mfms.dto.PaymentResponse;
import com.mfms.entity.ExternalBorrower;
import com.mfms.enums.BorrowerType;
import com.mfms.enums.EntityStatus;
import com.mfms.exception.ResourceNotFoundException;
import com.mfms.mapper.EntityMapper;
import com.mfms.repository.ExternalBorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalBorrowerService {

    private final ExternalBorrowerRepository borrowerRepository;
    private final LoanService loanService;
    private final EntityMapper mapper;
    private final ActivityLogService activityLogService;

    public List<ExternalBorrowerResponse> getAll() {
        return borrowerRepository.findAll().stream().map(mapper::toBorrowerResponse).toList();
    }

    public List<ExternalBorrowerResponse> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        return borrowerRepository.search(query).stream().map(mapper::toBorrowerResponse).toList();
    }

    public ExternalBorrowerResponse getById(Long id) {
        return mapper.toBorrowerResponse(findBorrower(id));
    }

    public ExternalBorrowerDetail getDetails(Long id) {
        ExternalBorrower borrower = findBorrower(id);
        List<LoanResponse> loans = loanService.getLoansByBorrower(BorrowerType.EXTERNAL, id);
        BigDecimal outstanding = loans.stream()
                .filter(l -> l.getStatus() == com.mfms.enums.LoanStatus.ACTIVE)
                .map(LoanResponse::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingInterest = loans.stream()
                .map(LoanResponse::getPendingInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentResponse> interestPayments = loans.stream()
                .flatMap(l -> loanService.getInterestPayments(l.getId()).stream())
                .toList();

        return ExternalBorrowerDetail.builder()
                .borrower(mapper.toBorrowerResponse(borrower))
                .outstandingAmount(outstanding)
                .pendingInterest(pendingInterest)
                .loans(loans)
                .interestPayments(interestPayments)
                .build();
    }

    @Transactional
    public ExternalBorrowerResponse create(ExternalBorrowerRequest request, Long adminId) {
        ExternalBorrower borrower = ExternalBorrower.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(EntityStatus.ACTIVE)
                .build();
        borrower = borrowerRepository.save(borrower);
        activityLogService.log(adminId, "Created External Borrower",
                "Borrower: " + borrower.getFullName());
        return mapper.toBorrowerResponse(borrower);
    }

    @Transactional
    public ExternalBorrowerResponse update(Long id, ExternalBorrowerRequest request, Long adminId) {
        ExternalBorrower borrower = findBorrower(id);
        borrower.setFullName(request.getFullName());
        borrower.setPhoneNumber(request.getPhoneNumber());
        borrower.setAddress(request.getAddress());
        if (request.getStatus() != null) borrower.setStatus(request.getStatus());
        borrower = borrowerRepository.save(borrower);
        activityLogService.log(adminId, "Updated External Borrower",
                "Borrower: " + borrower.getFullName());
        return mapper.toBorrowerResponse(borrower);
    }

    ExternalBorrower findBorrower(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));
    }

    @lombok.Data
    @lombok.Builder
    public static class ExternalBorrowerDetail {
        private ExternalBorrowerResponse borrower;
        private BigDecimal outstandingAmount;
        private BigDecimal pendingInterest;
        private List<LoanResponse> loans;
        private List<PaymentResponse> interestPayments;
    }
}
