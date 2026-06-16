package com.mfms.dto;

import com.mfms.enums.ContributionStatus;
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
public class ContributionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String memberCode;
    private Integer month;
    private Integer year;
    private BigDecimal amount;
    private ContributionStatus status;
    private LocalDate paymentDate;
    private LocalDateTime createdAt;
}
