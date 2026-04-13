package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.VoucherSeriesMaster;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherSeriesMasterRepository extends JpaRepository<VoucherSeriesMaster, String> {

    List<VoucherSeriesMaster> findByVoucherTypeId(String voucherTypeId);

    Page<VoucherSeriesMaster> findByVoucherTypeId(String voucherTypeId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM VoucherSeriesMaster s
            WHERE s.voucherTypeId = :voucherTypeId
            AND s.defaultSeries = true
            AND s.active = true
            LIMIT 1
            """)
    Optional<VoucherSeriesMaster> findDefaultSeriesForUpdate(
            @Param("voucherTypeId") String voucherTypeId);

    @Query("""
            SELECT s FROM VoucherSeriesMaster s
            WHERE s.voucherTypeId = :voucherTypeId
            AND s.defaultSeries = true
            AND s.active = true
            LIMIT 1
            """)
    Optional<VoucherSeriesMaster> findDefaultSeries(
            @Param("voucherTypeId") String voucherTypeId);

    @Query(value = "SELECT COUNT(*) FROM purchase_voucher_header WHERE voucher_number = :vn",
            nativeQuery = true)
    long countVoucherNumberUsage(@Param("vn") String voucherNumber);
}
