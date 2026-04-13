package com.countrydelight.mms.dto.outward;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {
    private Long issueId;
    private String branchId;
    private String branchName;
    private LocalDate issueDate;
    private String issuedTo;
    private String status;
    private String remarks;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private List<IssueDetailResponse> details;
    private LocalDateTime createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class IssueDetailResponse {
    private String itemId;
    private String itemDesc;
    private String locationId;
    private String locationName;
    private BigDecimal qtyIssued;
    private BigDecimal rate; // FIFO weighted average rate
    private List<FifoConsumptionResponse> fifoConsumptions;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class FifoConsumptionResponse {
    private Long grnId;
    private LocalDate grnDate;
    private BigDecimal qtyConsumed;
    private BigDecimal rate;
}
