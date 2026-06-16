package com.mfms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanUpdateRequest {
    @NotNull
    @DecimalMin("1.0")
    private BigDecimal loanAmount;

    private LocalDate loanDate;
}
