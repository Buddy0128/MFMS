package com.mfms.repository;

import com.mfms.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop20ByOrderByCreatedAtDesc();
    List<ActivityLog> findByAdminIdOrderByCreatedAtDesc(Long adminId);
}
