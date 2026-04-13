package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.BranchItemPrice;
import com.countrydelight.mms.entity.master.BranchItemPriceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchItemPriceRepository extends JpaRepository<BranchItemPrice, BranchItemPriceId> {
    List<BranchItemPrice> findByItemId(String itemId);
    Page<BranchItemPrice> findByItemId(String itemId, Pageable pageable);
    List<BranchItemPrice> findByBranchId(String branchId);
    Optional<BranchItemPrice> findByBranchIdAndItemId(String branchId, String itemId);
}
