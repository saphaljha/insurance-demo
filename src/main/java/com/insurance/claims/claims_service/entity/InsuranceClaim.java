package com.insurance.claims.claims_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "insurance_claim",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_claim_duplicate",
                        columnNames = {"customer_id", "policy_number", "claim_type", "incident_date"}
                )
        }
)
public class InsuranceClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "policy_number", nullable = false)
    private String policyNumber;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @NotNull
    @Column(name = "claim_type", nullable = false)
    private String claimType;

    @NotNull
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @NotNull
    @Column(name = "claimed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @NotNull
    @Column(name = "description", nullable = false, length = 1000)
    private String claimDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private ClaimStatus currentStatus = ClaimStatus.SUBMITTED;

    @Column(name = "reviewer_id")
    private String reviewerId;

    @Column(name = "reviewer_remarks", length = 500)
    private String reviewerRemarks;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.currentStatus == null) {
            this.currentStatus = ClaimStatus.SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}