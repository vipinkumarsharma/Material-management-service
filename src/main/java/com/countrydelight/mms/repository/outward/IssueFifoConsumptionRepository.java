package com.countrydelight.mms.repository.outward;

import com.countrydelight.mms.entity.outward.IssueFifoConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueFifoConsumptionRepository extends JpaRepository<IssueFifoConsumption, Long> {
    List<IssueFifoConsumption> findByIssueIdAndItemId(Long issueId, String itemId);
    List<IssueFifoConsumption> findByGrnIdAndItemId(Long grnId, String itemId);
}
