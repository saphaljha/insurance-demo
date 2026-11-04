package com.insurance.claims.claims_service.workflow;

import com.insurance.claims.claims_service.entity.ClaimStatus;
import org.springframework.stereotype.Component;

@Component
public class ClaimWorkflowEngine {

    public void validateTransition(ClaimStatus currentStatus, ClaimStatus targetStatus) {
        boolean isValid = switch (currentStatus) {
            case SUBMITTED -> targetStatus == ClaimStatus.UNDER_REVIEW;
            case UNDER_REVIEW -> targetStatus == ClaimStatus.APPROVED || targetStatus == ClaimStatus.REJECTED;
            case REJECTED -> targetStatus == ClaimStatus.UNDER_REVIEW;
            case APPROVED -> false; // Final terminal state
        };

        if (!isValid) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", currentStatus, targetStatus)
            );
        }
    }
}