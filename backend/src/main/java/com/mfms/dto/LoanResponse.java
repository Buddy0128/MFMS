package com.mfms.dto;

import com.mfms.enums.BorrowerType;
import com.mfms.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private BorrowerType borrowerType;
    private Long borrowerId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal interestRate;
    private BigDecimal monthlyInterest;
    private BigDecimal pendingInterest;
    private BigDecimal totalInterestPaid;
    private LocalDate loanDate;
    private LoanStatus status;
    private LocalDateTime createdAt;
}
