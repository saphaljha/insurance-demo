package com.insurance.claims.claims_service.service;

import com.insurance.claims.claims_service.dto.ClaimRequestDTO;
import com.insurance.claims.claims_service.dto.DecisionRequestDTO;
import com.insurance.claims.claims_service.dto.ReviewRequestDTO;
import com.insurance.claims.claims_service.entity.ClaimStatus;
import com.insurance.claims.claims_service.entity.ClaimStatusHistory;
import com.insurance.claims.claims_service.entity.InsuranceClaim;
import com.insurance.claims.claims_service.exception.ClaimNotFoundException;
import com.insurance.claims.claims_service.exception.DuplicateClaimException;
import com.insurance.claims.claims_service.exception.InvalidTransitionException;
import com.insurance.claims.claims_service.repository.ClaimHistoryRepository;
import com.insurance.claims.claims_service.repository.ClaimRepository;
import com.insurance.claims.claims_service.workflow.ClaimWorkflowEngine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimHistoryRepository historyRepository;
    private final ClaimWorkflowEngine workflowEngine;

    public ClaimService(ClaimRepository claimRepository,
                        ClaimHistoryRepository historyRepository,
                        ClaimWorkflowEngine workflowEngine) {
        this.claimRepository = claimRepository;
        this.historyRepository = historyRepository;
        this.workflowEngine = workflowEngine;
    }

    @Transactional
    public InsuranceClaim createClaim(ClaimRequestDTO request) {
        // 1. Check for duplicate submission based on exact criteria
        Optional<InsuranceClaim> existing = claimRepository.findByCustomerIdAndPolicyNumberAndClaimTypeAndIncidentDate(
                request.customerId(), request.policyNumber(), request.claimType(), request.incidentDate()
        );

        if (existing.isPresent()) {
            throw new DuplicateClaimException(
                    "Duplicate claim detected. A claim with the same customer ID, policy number, type, and incident date already exists.",
                    existing.get().getId()
            );
        }

        // 2. Map DTO to entity
        InsuranceClaim claim = new InsuranceClaim();
        claim.setPolicyNumber(request.policyNumber());
        claim.setCustomerId(request.customerId());
        claim.setClaimType(request.claimType());
        claim.setIncidentDate(request.incidentDate());
        claim.setClaimedAmount(request.claimedAmount());
        claim.setClaimDescription(request.claimDescription());
        claim.setCurrentStatus(ClaimStatus.SUBMITTED);

        InsuranceClaim savedClaim = claimRepository.save(claim);

        // 3. Record initial status history entry
        recordHistory(savedClaim.getId(), null, ClaimStatus.SUBMITTED, "Initial claim creation", request.customerId());

        return savedClaim;
    }

    public InsuranceClaim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException("Claim not found with ID: " + id));
    }

    public Page<InsuranceClaim> listClaims(ClaimStatus status, Pageable pageable) {
        if (status != null) {
            return claimRepository.findByCurrentStatus(status, pageable);
        }
        return claimRepository.findAll(pageable);
    }

    @Transactional
    public InsuranceClaim reviewClaim(Long id, ReviewRequestDTO request) {
        InsuranceClaim claim = getClaimById(id);

        ClaimStatus currentStatus = claim.getCurrentStatus();
        ClaimStatus targetStatus = ClaimStatus.UNDER_REVIEW;

        workflowEngine.validateTransition(currentStatus, targetStatus);

        claim.setCurrentStatus(targetStatus);
        claim.setReviewerId(request.reviewerId());
        claim.setReviewerRemarks(request.remarks());

        InsuranceClaim updatedClaim = claimRepository.save(claim);

        recordHistory(id, currentStatus, targetStatus, request.remarks(), request.reviewerId());

        return updatedClaim;
    }

    @Transactional
    public InsuranceClaim makeDecision(Long id, DecisionRequestDTO request) {
        InsuranceClaim claim = getClaimById(id);

        if (claim.getCurrentStatus() != ClaimStatus.UNDER_REVIEW) {
            throw new InvalidTransitionException("A decision (approve or reject) can only be made on a claim that is UNDER_REVIEW.");
        }

        ClaimStatus targetStatus;

        if ("APPROVED".equalsIgnoreCase(request.decision())) {
            targetStatus = ClaimStatus.APPROVED;

            if (request.approvedAmount() == null || request.approvedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Approved amount must be greater than zero.");
            }
            if (request.approvedAmount().compareTo(claim.getClaimedAmount()) > 0) {
                throw new IllegalArgumentException("Approved amount must not exceed the claimed amount.");
            }

            claim.setApprovedAmount(request.approvedAmount());
            claim.setReviewerRemarks(request.rejectionReason() != null ? request.rejectionReason() : "Claim approved.");

        } else if ("REJECTED".equalsIgnoreCase(request.decision())) {
            targetStatus = ClaimStatus.REJECTED;

            if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
                throw new IllegalArgumentException("A rejection reason is mandatory when rejecting a claim.");
            }

            claim.setReviewerRemarks(request.rejectionReason());
        } else {
            throw new IllegalArgumentException("Invalid decision type. Must be APPROVED or REJECTED.");
        }

        workflowEngine.validateTransition(claim.getCurrentStatus(), targetStatus);

        ClaimStatus previousStatus = claim.getCurrentStatus();
        claim.setCurrentStatus(targetStatus);

        InsuranceClaim updatedClaim = claimRepository.save(claim);

        recordHistory(id, previousStatus, targetStatus, claim.getReviewerRemarks(), request.decidedBy());

        return updatedClaim;
    }

    public List<ClaimStatusHistory> getClaimHistory(Long id) {
        getClaimById(id);
        return historyRepository.findByClaimIdOrderByChangedAtAsc(id);
    }

    private void recordHistory(Long claimId, ClaimStatus prevStatus, ClaimStatus newStatus, String remarks, String changedBy) {
        ClaimStatusHistory history = new ClaimStatusHistory();
        history.setClaimId(claimId);
        history.setPreviousStatus(prevStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }
}