package com.insurance.claims.claims_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DecisionRequestDTO(
        @NotNull(message = "Decision action is required (APPROVED or REJECTED)")
        String decision,

        @Positive(message = "Approved amount must be positive")
        BigDecimal approvedAmount,

        String rejectionReason,

        @NotNull(message = "Decided by user ID is required")
        String decidedBy
) {}