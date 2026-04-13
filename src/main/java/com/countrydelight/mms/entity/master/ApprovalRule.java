package com.countrydelight.mms.entity.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRule {

    @Id
    @Column(name = "rule_id", length = 20)
    private String ruleId;

    @Column(name = "txn_type", nullable = false, length = 20)
    private String txnType; // GRN / ISSUE / TRANSFER

    @Column(name = "condition_type", nullable = false, length = 30)
    private String conditionType; // PRICE_VARIANCE / QTY_VARIANCE

    @Column(name = "threshold_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "required_role", nullable = false, length = 20)
    private String requiredRole;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_role", insertable = false, updatable = false)
    private RoleMaster role;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
