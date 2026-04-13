package com.countrydelight.mms.entity.outward;

import com.countrydelight.mms.entity.inward.GrnDetail;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
@Table(name = "issue_fifo_consumption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueFifoConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumption_id")
    private Long consumptionId;

    @Column(name = "issue_id", nullable = false)
    private Long issueId;

    @Column(name = "item_id", nullable = false, length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "issue_id", referencedColumnName = "issue_id", insertable = false, updatable = false),
        @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    })
    private IssueDetail issueDetail;

    @Column(name = "grn_id", nullable = false)
    private Long grnId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "grn_id", referencedColumnName = "grn_id", insertable = false, updatable = false),
        @JoinColumn(name = "item_id", referencedColumnName = "item_id", insertable = false, updatable = false)
    })
    private GrnDetail grnDetail;

    @Column(name = "qty_consumed", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtyConsumed;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    private BigDecimal rate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
