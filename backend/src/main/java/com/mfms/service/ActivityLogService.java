package com.mfms.service;

import com.mfms.dto.ActivityLogResponse;
import com.mfms.entity.ActivityLog;
import com.mfms.entity.Admin;
import com.mfms.repository.ActivityLogRepository;
import com.mfms.repository.AdminRepository;
import com.mfms.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final AdminRepository adminRepository;
    private final EntityMapper mapper;

    @Transactional
    public void log(Long adminId, String action, String description) {
        Admin admin = adminId != null ? adminRepository.findById(adminId).orElse(null) : null;
        ActivityLog log = ActivityLog.builder()
                .admin(admin)
                .action(action)
                .description(description)
                .build();
        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getRecentLogs() {
        return activityLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(mapper::toActivityLogResponse)
                .toList();
    }

    public List<ActivityLogResponse> getAllLogs() {
        return activityLogRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(mapper::toActivityLogResponse)
                .toList();
    }
}
