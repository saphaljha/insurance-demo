package com.insurance.claims.claims_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ClaimRequestDTO(
        @NotBlank(message = "Policy number is required")
        String policyNumber,

        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotBlank(message = "Claim type is required")
        String claimType,

        @NotNull(message = "Incident date is required")
        LocalDate incidentDate,

        @NotNull(message = "Claimed amount is required")
        @Positive(message = "Claimed amount must be greater than zero")
        BigDecimal claimedAmount,

        @NotBlank(message = "Claim description is required")
        String claimDescription
) {}