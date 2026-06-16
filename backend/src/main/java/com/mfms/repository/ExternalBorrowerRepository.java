package com.mfms.repository;

import com.mfms.entity.ExternalBorrower;
import com.mfms.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExternalBorrowerRepository extends JpaRepository<ExternalBorrower, Long> {
    long countByStatus(EntityStatus status);

    @Query("SELECT b FROM ExternalBorrower b WHERE " +
           "LOWER(b.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "b.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<ExternalBorrower> search(@Param("query") String query);

    List<ExternalBorrower> findByStatus(EntityStatus status);
}
