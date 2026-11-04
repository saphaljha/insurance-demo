package com.insurance.claims.claims_service.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Long existingClaimId // Populated only for duplicate conflicts
) {}