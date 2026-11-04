package com.insurance.claims.claims_service.repository;

import com.insurance.claims.claims_service.entity.ClaimStatus;
import com.insurance.claims.claims_service.entity.InsuranceClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    // Used for duplicate protection check: finds an existing claim matching all 4 fields
    Optional<InsuranceClaim> findByCustomerIdAndPolicyNumberAndClaimTypeAndIncidentDate(
            String customerId,
            String policyNumber,
            String claimType,
            LocalDate incidentDate
    );

    // Used for listing and filtering claims by status with pagination & sorting support
    Page<InsuranceClaim> findByCurrentStatus(ClaimStatus currentStatus, Pageable pageable);
}