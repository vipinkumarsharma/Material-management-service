package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.GodownStockResponse;
import com.countrydelight.mms.entity.master.GodownItemStock;
import com.countrydelight.mms.entity.master.SupplierGodownMap;
import com.countrydelight.mms.entity.purchase.PurchaseVoucherDetail;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.GodownItemStockRepository;
import com.countrydelight.mms.repository.master.SupplierGodownMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GodownItemStockService {

    private static final Set<String> GODOWN_DECREASES = Set.of("Receipt Note", "Material In", "Rejections In");
    private static final Set<String> GODOWN_INCREASES = Set.of("Delivery Note", "Material Out", "Rejections Out");

    private final GodownItemStockRepository godownItemStockRepository;
    private final SupplierGodownMapRepository supplierGodownMapRepository;

    @Transactional
    public void addStock(Long godownId, String itemId, BigDecimal qty) {
        GodownItemStock stock = godownItemStockRepository
                .findById(new com.countrydelight.mms.entity.master.GodownItemStockId(godownId, itemId))
                .orElseGet(() -> GodownItemStock.builder()
                        .godownId(godownId)
                        .itemId(itemId)
                        .qty(BigDecimal.ZERO)
                        .build());
        stock.setQty(stock.getQty().add(qty));
        godownItemStockRepository.save(stock);
    }

    @Transactional
    public void removeStock(Long godownId, String itemId, BigDecimal qty) {
        GodownItemStock stock = godownItemStockRepository
                .findById(new com.countrydelight.mms.entity.master.GodownItemStockId(godownId, itemId))
                .orElseGet(() -> GodownItemStock.builder()
                        .godownId(godownId)
                        .itemId(itemId)
                        .qty(BigDecimal.ZERO)
                        .build());
        BigDecimal newQty = stock.getQty().subtract(qty);
        stock.setQty(newQty.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newQty);
        godownItemStockRepository.save(stock);
    }

    @Transactional(readOnly = true)
    public Page<GodownStockResponse> getStockBySupplier(String suppId, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<SupplierGodownMap> godownsPage = supplierGodownMapRepository.findBySuppId(suppId, pageable);
        List<GodownStockResponse> content = godownsPage.getContent().stream().map(sgm -> {
            List<GodownItemStock> stockItems = godownItemStockRepository.findByGodownId(sgm.getId());
            List<GodownStockResponse.GodownItemStockEntry> entries = stockItems.stream()
                    .map(s -> GodownStockResponse.GodownItemStockEntry.builder()
                            .itemId(s.getItemId())
                            .qty(s.getQty())
                            .build())
                    .collect(Collectors.toList());
            return GodownStockResponse.builder()
                    .id(sgm.getId())
                    .godownName(sgm.getGodownName())
                    .suppId(sgm.getSuppId())
                    .items(entries)
                    .build();
        }).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, godownsPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public GodownStockResponse getStockByGodown(Long godownId, String suppId) {
        SupplierGodownMap sgm = supplierGodownMapRepository.findById(godownId)
                .orElseThrow(() -> new MmsException("Godown not found: " + godownId));
        if (suppId != null && !suppId.equals(sgm.getSuppId())) {
            throw new MmsException("Godown " + godownId + " does not belong to supplier " + suppId);
        }
        List<GodownItemStock> stockItems = godownItemStockRepository.findByGodownId(godownId);
        List<GodownStockResponse.GodownItemStockEntry> entries = stockItems.stream()
                .map(s -> GodownStockResponse.GodownItemStockEntry.builder()
                        .itemId(s.getItemId()).qty(s.getQty()).build())
                .collect(Collectors.toList());
        return GodownStockResponse.builder()
                .id(sgm.getId()).godownName(sgm.getGodownName())
                .suppId(sgm.getSuppId()).items(entries).build();
    }

    @Transactional
    public void applyGodownStock(Long godownId, String category,
                                 List<PurchaseVoucherDetail> details, boolean isReversal) {
        if (godownId == null || category == null || details == null) {
            return;
        }
        boolean decreases = GODOWN_DECREASES.contains(category);
        boolean increases = GODOWN_INCREASES.contains(category);
        if (!decreases && !increases) {
            return; // Skip Job Work — handled by applyJobWork
        }

        // Normal: decreases → removeStock; increases → addStock. Reversal: flip.
        boolean doAdd = isReversal ? decreases : increases;

        for (PurchaseVoucherDetail detail : details) {
            if (doAdd) {
                addStock(godownId, detail.getItemId(), detail.getQty());
            } else {
                removeStock(godownId, detail.getItemId(), detail.getQty());
            }
        }
    }
}
