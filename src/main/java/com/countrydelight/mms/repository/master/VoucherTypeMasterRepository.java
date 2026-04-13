package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.VoucherTypeMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Sort;

@Repository
public interface VoucherTypeMasterRepository extends JpaRepository<VoucherTypeMaster, String> {

    List<VoucherTypeMaster> findByReportSummaryTitleIsNotNull(Sort sort);

    Page<VoucherTypeMaster> findByVoucherCategory(String voucherCategory, Pageable pageable);

    List<VoucherTypeMaster> findByActiveTrue();

    List<VoucherTypeMaster> findByVoucherCategoryAndActiveTrue(String voucherCategory);

    @Query("SELECT vt FROM VoucherTypeMaster vt WHERE :branchId MEMBER OF vt.branchIds")
    Page<VoucherTypeMaster> findByBranchId(@Param("branchId") String branchId, Pageable pageable);

    @Query("SELECT vt FROM VoucherTypeMaster vt WHERE :branchId MEMBER OF vt.branchIds AND vt.voucherCategory = :voucherCategory")
    Page<VoucherTypeMaster> findByBranchIdAndVoucherCategory(@Param("branchId") String branchId, @Param("voucherCategory") String voucherCategory, Pageable pageable);
}
