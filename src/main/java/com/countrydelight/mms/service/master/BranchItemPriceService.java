package com.countrydelight.mms.service.master;

import com.countrydelight.mms.dto.master.BranchItemPriceRequest;
import com.countrydelight.mms.entity.master.BranchItemPrice;
import com.countrydelight.mms.entity.master.BranchItemPriceId;
import com.countrydelight.mms.exception.MmsException;
import com.countrydelight.mms.repository.master.BranchItemPriceRepository;
import com.countrydelight.mms.repository.master.BranchMasterRepository;
import com.countrydelight.mms.repository.master.ItemMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchItemPriceService {

    private final BranchItemPriceRepository branchItemPriceRepository;
    private final BranchMasterRepository branchMasterRepository;
    private final ItemMasterRepository itemMasterRepository;

    public Page<BranchItemPrice> getBranchPricesForItem(String itemId, String branchId, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        if (StringUtils.hasText(branchId)) {
            Optional<BranchItemPrice> price = branchItemPriceRepository.findByBranchIdAndItemId(branchId, itemId);
            List<BranchItemPrice> list = price.map(List::of).orElse(List.of());
            return new PageImpl<>(list, pageable, list.size());
        }
        return branchItemPriceRepository.findByItemId(itemId, pageable);
    }

    @Transactional
    public BranchItemPrice setBranchPrice(String branchId, String itemId, BranchItemPriceRequest request) {
        // Validate branch and item exist
        if (!branchMasterRepository.existsById(branchId)) {
            throw new MmsException("Branch not found: " + branchId);
        }
        if (!itemMasterRepository.existsById(itemId)) {
            throw new MmsException("Item not found: " + itemId);
        }

        BranchItemPrice price = branchItemPriceRepository.findByBranchIdAndItemId(branchId, itemId)
                .orElse(BranchItemPrice.builder()
                        .branchId(branchId)
                        .itemId(itemId)
                        .build());

        price.setCostPrice(request.getCostPrice());
        price.setMrp(request.getMrp() != null ? request.getMrp() : request.getCostPrice());
        return branchItemPriceRepository.save(price);
    }

    @Transactional
    public void removeBranchPrice(String branchId, String itemId) {
        BranchItemPriceId id = new BranchItemPriceId(branchId, itemId);
        if (!branchItemPriceRepository.existsById(id)) {
            throw new MmsException("Branch price not found: " + branchId + "/" + itemId);
        }
        branchItemPriceRepository.deleteById(id);
    }

    /**
     * Get effective cost price for an item at a branch.
     * Falls back to item_master.cost_price if no branch override exists.
     */
    public BigDecimal getEffectiveCostPrice(String branchId, String itemId) {
        Optional<BranchItemPrice> branchPrice = branchItemPriceRepository.findByBranchIdAndItemId(branchId, itemId);
        if (branchPrice.isPresent()) {
            return branchPrice.get().getCostPrice();
        }
        return itemMasterRepository.findById(itemId)
                .map(item -> item.getCostPrice())
                .orElse(null);
    }

    /**
     * Get branch-level cost price only (no fallback).
     */
    public BigDecimal getBranchCostPrice(String branchId, String itemId) {
        return branchItemPriceRepository.findByBranchIdAndItemId(branchId, itemId)
                .map(BranchItemPrice::getCostPrice)
                .orElse(null);
    }
}
