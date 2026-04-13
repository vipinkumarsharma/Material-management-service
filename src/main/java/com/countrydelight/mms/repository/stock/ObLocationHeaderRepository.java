package com.countrydelight.mms.repository.stock;

import com.countrydelight.mms.entity.stock.ObLocationHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObLocationHeaderRepository extends JpaRepository<ObLocationHeader, Long> {

    Optional<ObLocationHeader> findByBranchIdAndLocationId(String branchId, String locationId);
}
