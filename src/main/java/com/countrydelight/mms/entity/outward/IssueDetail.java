package com.countrydelight.mms.entity.outward;

import com.countrydelight.mms.entity.master.ItemMaster;
import com.countrydelight.mms.entity.master.LocationMaster;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issue_detail")
@IdClass(IssueDetailId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueDetail {

    @Id
    @Column(name = "issue_id")
    private Long issueId;

    @Id
    @Column(name = "item_id", length = 20)
    private String itemId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", insertable = false, updatable = false)
    private IssueHeader issueHeader;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster item;

    @Column(name = "location_id", nullable = false, length = 20)
    private String locationId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private LocationMaster location;

    @Column(name = "qty_issued", nullable = false, precision = 15, scale = 4)
    private BigDecimal qtyIssued;

    @Column(name = "rate", nullable = false, precision = 15, scale = 4)
    private BigDecimal rate; // Derived from FIFO GRN rate (weighted average of consumed batches)

    @OneToMany(mappedBy = "issueDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IssueFifoConsumption> fifoConsumptions = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
