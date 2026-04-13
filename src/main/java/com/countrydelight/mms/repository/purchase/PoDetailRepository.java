package com.countrydelight.mms.repository.purchase;

import com.countrydelight.mms.entity.purchase.PoDetail;
import com.countrydelight.mms.entity.purchase.PoDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoDetailRepository extends JpaRepository<PoDetail, PoDetailId> {
    List<PoDetail> findByPoId(Long poId);

    @Query("""
            SELECT pd FROM PoDetail pd
            WHERE pd.poId = :poId AND pd.qtyOrdered > pd.qtyReceived
            """)
    List<PoDetail> findPendingItemsByPoId(Long poId);
}
