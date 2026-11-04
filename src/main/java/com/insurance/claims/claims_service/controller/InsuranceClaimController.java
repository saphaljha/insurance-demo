package com.insurance.claims.claims_service.controller;

import com.insurance.claims.claims_service.dto.ClaimRequestDTO;
import com.insurance.claims.claims_service.dto.DecisionRequestDTO;
import com.insurance.claims.claims_service.dto.ReviewRequestDTO;
import com.insurance.claims.claims_service.entity.ClaimStatus;
import com.insurance.claims.claims_service.entity.ClaimStatusHistory;
import com.insurance.claims.claims_service.entity.InsuranceClaim;
import com.insurance.claims.claims_service.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class InsuranceClaimController {

    private final ClaimService claimService;

    // Explicit constructor to initialize the final service dependency
    public InsuranceClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    // 1. Create a Claim (POST /api/claims)
    @PostMapping
    public ResponseEntity<InsuranceClaim> createClaim(@Valid @RequestBody ClaimRequestDTO request) {
        InsuranceClaim createdClaim = claimService.createClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClaim);
    }

    // 2. Retrieve a Claim by ID (GET /api/claims/{claimId})
    @GetMapping("/{claimId}")
    public ResponseEntity<InsuranceClaim> getClaimById(@PathVariable Long claimId) {
        InsuranceClaim claim = claimService.getClaimById(claimId);
        return ResponseEntity.ok(claim);
    }

    // 3. List and Filter Claims with Pagination (GET /api/claims)
    @GetMapping
    public ResponseEntity<Page<InsuranceClaim>> listClaims(
            @RequestParam(required = false) ClaimStatus status,
            Pageable pageable) {
        Page<InsuranceClaim> claims = claimService.listClaims(status, pageable);
        return ResponseEntity.ok(claims);
    }

    // 4. Review a Claim (PATCH /api/claims/{claimId}/review)
    @PatchMapping("/{claimId}/review")
    public ResponseEntity<InsuranceClaim> reviewClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ReviewRequestDTO request) {
        InsuranceClaim updatedClaim = claimService.reviewClaim(claimId, request);
        return ResponseEntity.ok(updatedClaim);
    }

    // 5. Approve or Reject a Claim (PATCH /api/claims/{claimId}/decision)
    @PatchMapping("/{claimId}/decision")
    public ResponseEntity<InsuranceClaim> makeDecision(
            @PathVariable Long claimId,
            @Valid @RequestBody DecisionRequestDTO request) {
        InsuranceClaim updatedClaim = claimService.makeDecision(claimId, request);
        return ResponseEntity.ok(updatedClaim);
    }

    // 6. Get Status History Audit Trail (GET /api/claims/{claimId}/history)
    @GetMapping("/{claimId}/history")
    public ResponseEntity<List<ClaimStatusHistory>> getClaimHistory(@PathVariable Long claimId) {
        List<ClaimStatusHistory> history = claimService.getClaimHistory(claimId);
        return ResponseEntity.ok(history);
    }
}