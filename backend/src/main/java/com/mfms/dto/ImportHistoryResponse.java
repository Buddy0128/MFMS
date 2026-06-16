package com.mfms.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryResponse {
    private Long id;
    private String fileName;
    private int totalRows;
    private int newMembers;
    private int updatedMembers;
    private int failedRecords;
    private String importedBy;
    private LocalDateTime importedAt;
}
