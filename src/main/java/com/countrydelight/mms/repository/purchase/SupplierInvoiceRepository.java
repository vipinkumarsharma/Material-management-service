package com.countrydelight.mms.repository.purchase;

import com.countrydelight.mms.entity.purchase.SupplierInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, Long> {

    @Query("SELECT s FROM SupplierInvoice s WHERE s.suppId = :suppId")
    Page<SupplierInvoice> findBySuppId(@Param("suppId") String suppId, Pageable pageable);

    @Query("SELECT s FROM SupplierInvoice s WHERE s.invoiceDate BETWEEN :startDate AND :endDate")
    Page<SupplierInvoice> findByInvoiceDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    Optional<SupplierInvoice> findBySuppIdAndInvoiceNo(String suppId, String invoiceNo);
}
