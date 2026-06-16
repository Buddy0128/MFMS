package com.mfms.dto;

import com.mfms.enums.ContributionStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContributionRequest {
    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull @Min(1) @Max(12)
    private Integer month;

    @NotNull @Min(2000)
    private Integer year;

    private BigDecimal amount;

    @NotNull
    private ContributionStatus status;

    private LocalDate paymentDate;
}
