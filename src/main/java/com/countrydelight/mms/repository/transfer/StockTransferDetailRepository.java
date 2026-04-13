package com.countrydelight.mms.repository.transfer;

import com.countrydelight.mms.entity.transfer.StockTransferDetail;
import com.countrydelight.mms.entity.transfer.StockTransferDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockTransferDetailRepository extends JpaRepository<StockTransferDetail, StockTransferDetailId> {
    List<StockTransferDetail> findByTransferId(Long transferId);
}
