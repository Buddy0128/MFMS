package com.mfms.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private int totalRowsProcessed;
    private int newMembersAdded;
    private int existingMembersUpdated;
    private int failedRecords;
    private LocalDateTime importDateTime;
    private List<String> errors;
}
