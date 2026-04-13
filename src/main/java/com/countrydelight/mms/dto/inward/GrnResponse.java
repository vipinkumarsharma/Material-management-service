package com.countrydelight.mms.dto.inward;

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
public class GrnResponse {
    private Long grnId;
    private String branchId;
    private String branchName;
    private String suppId;
    private String suppName;
    private Long pvId;
    private Long invoiceId;
    private String challanNo;
    private LocalDate challanDate;
    private LocalDate grnDate;
    private String status;
    private String remarks;
    private BigDecimal itemsCount;
    private BigDecimal totalQty;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private List<GrnDetailResponse> details;
    private LocalDateTime createdAt;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GrnDetailResponse {
    private String itemId;
    private String itemDesc;
    private String unitId;
    private String locationId;
    private String locationName;
    private BigDecimal qtyReceived;
    private BigDecimal rate;
    private BigDecimal grossAmount;
    private BigDecimal gstPerc;
    private BigDecimal gstAmount;
    private BigDecimal discountPerc;
    private BigDecimal discountAmount;
    private BigDecimal cessPerc;
    private BigDecimal cessAmount;
    private BigDecimal netAmount;
    private BigDecimal qtyRemaining;
}
