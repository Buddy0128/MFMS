package com.mfms.dto;

import com.mfms.enums.BorrowerType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanRequest {
    @NotNull
    private BorrowerType borrowerType;

    @NotNull
    private Long borrowerId;

    @NotNull @DecimalMin("1.0")
    private BigDecimal loanAmount;

    private LocalDate loanDate;
}
