package com.mfms.dto;

import com.mfms.enums.EntityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBorrowerResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String address;
    private EntityStatus status;
    private LocalDateTime createdAt;
}
