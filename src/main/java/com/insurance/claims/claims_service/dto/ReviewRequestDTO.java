package com.insurance.claims.claims_service.dto;

import jakarta.validation.constraints.NotBlank;

public record ReviewRequestDTO(
        @NotBlank(message = "Reviewer ID/Username is required")
        String reviewerId,

        @NotBlank(message = "Remarks are required")
        String remarks
) {}