package com.insurance.claims.claims_service.repository;

import com.insurance.claims.claims_service.entity.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {

    // Retrieves all status history records for a given claim ID ordered chronologically
    List<ClaimStatusHistory> findByClaimIdOrderByChangedAtAsc(Long claimId);
}