package com.insurance.claims.claims_service.exception;

public class DuplicateClaimException extends RuntimeException {
    private final Long existingClaimId;

    public DuplicateClaimException(String message, Long existingClaimId) {
        super(message);
        this.existingClaimId = existingClaimId;
    }

    public Long getExistingClaimId() {
        return existingClaimId;
    }
}