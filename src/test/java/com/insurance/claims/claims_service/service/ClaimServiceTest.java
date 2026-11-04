package com.insurance.claims.claims_service.service;

import com.insurance.claims.claims_service.dto.ClaimRequestDTO;
import com.insurance.claims.claims_service.entity.ClaimStatus;
import com.insurance.claims.claims_service.entity.InsuranceClaim;
import com.insurance.claims.claims_service.exception.DuplicateClaimException;
import com.insurance.claims.claims_service.repository.ClaimHistoryRepository;
import com.insurance.claims.claims_service.repository.ClaimRepository;
import com.insurance.claims.claims_service.workflow.ClaimWorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClaimServiceTest {

    private ClaimRepository claimRepository;
    private ClaimHistoryRepository historyRepository;
    private ClaimWorkflowEngine workflowEngine;
    private ClaimService claimService;

    private ClaimRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        // Create mocks for repositories manually
        claimRepository = mock(ClaimRepository.class);
        historyRepository = mock(ClaimHistoryRepository.class);

        // Use a real instance of the workflow engine with no Mockito wrapping
        workflowEngine = new ClaimWorkflowEngine();

        // Instantiate the service manually
        claimService = new ClaimService(claimRepository, historyRepository, workflowEngine);

        sampleRequest = new ClaimRequestDTO(
                "POL-12345",
                "CUST-001",
                "AUTO",
                LocalDate.of(2026, 11, 1),
                new BigDecimal("1500.00"),
                "Car bumper damage in parking lot."
        );
    }

    @Test
    void testCreateClaim_Success() {
        // Arrange: No existing duplicate found
        when(claimRepository.findByCustomerIdAndPolicyNumberAndClaimTypeAndIncidentDate(
                any(), any(), any(), any()
        )).thenReturn(Optional.empty());

        InsuranceClaim savedMockClaim = new InsuranceClaim();
        savedMockClaim.setId(10L);
        savedMockClaim.setCustomerId(sampleRequest.customerId());
        savedMockClaim.setPolicyNumber(sampleRequest.policyNumber());
        savedMockClaim.setCurrentStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.save(any(InsuranceClaim.class))).thenReturn(savedMockClaim);

        // Act
        InsuranceClaim result = claimService.createClaim(sampleRequest);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(ClaimStatus.SUBMITTED, result.getCurrentStatus());
        verify(claimRepository, times(1)).save(any(InsuranceClaim.class));
        verify(historyRepository, times(1)).save(any());
    }

    @Test
    void testCreateClaim_DuplicateThrowsException() {
        // Arrange: Simulate that a duplicate claim already exists in the DB
        InsuranceClaim existingClaim = new InsuranceClaim();
        existingClaim.setId(42L);

        when(claimRepository.findByCustomerIdAndPolicyNumberAndClaimTypeAndIncidentDate(
                any(), any(), any(), any()
        )).thenReturn(Optional.of(existingClaim));

        // Act & Assert
        DuplicateClaimException exception = assertThrows(
                DuplicateClaimException.class,
                () -> claimService.createClaim(sampleRequest)
        );

        assertEquals(42L, exception.getExistingClaimId());
        verify(claimRepository, never()).save(any(InsuranceClaim.class));
    }
}