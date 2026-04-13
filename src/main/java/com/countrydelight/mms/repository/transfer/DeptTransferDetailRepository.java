package com.countrydelight.mms.repository.transfer;

import com.countrydelight.mms.entity.transfer.DeptTransferDetail;
import com.countrydelight.mms.entity.transfer.DeptTransferDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptTransferDetailRepository extends JpaRepository<DeptTransferDetail, DeptTransferDetailId> {
    List<DeptTransferDetail> findByDeptTransferId(Long deptTransferId);
}
