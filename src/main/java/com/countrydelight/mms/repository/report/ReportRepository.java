package com.countrydelight.mms.repository.report;

import com.countrydelight.mms.entity.stock.MaterialStockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for all report queries.
 * All queries support optional branch filtering:
 * - If branchIds is NULL or empty: returns data for ALL branches
 * - If branchIds has values: returns data for specified branches only
 */
@Repository
public interface ReportRepository extends JpaRepository<MaterialStockLedger, Long> {

    // ========================================================================
    // REPORT 1: CURRENT STOCK REPORT
    // ========================================================================
    /**
     * Current stock with branch, item, location details.
     * Indexes used: branch_material_stock(branch_id), item_master(item_id)
     */
    @Query(value = """
        SELECT
            bms.branch_id,
            b.branch_name,
            bms.item_id,
            i.item_desc,
            bms.location_id,
            l.location_name,
            bms.qty_on_hand,
            bms.avg_cost,
            (bms.qty_on_hand * bms.avg_cost) as stock_value
        FROM branch_material_stock bms
        JOIN branch_master b ON bms.branch_id = b.branch_id
        JOIN item_master i ON bms.item_id = i.item_id
        LEFT JOIN location_master l ON bms.location_id = l.location_id
        WHERE (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR bms.item_id = :itemId)
          AND (:locationId IS NULL OR bms.location_id = :locationId)
          AND bms.qty_on_hand > 0
        ORDER BY bms.branch_id, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findCurrentStock(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("locationId") String locationId
    );

    // ========================================================================
    // REPORT 2: CONSOLIDATED STOCK SUMMARY
    // ========================================================================
    /**
     * Total stock per item across all/selected branches.
     */
    @Query(value = """
        SELECT
            bms.item_id,
            i.item_desc,
            SUM(bms.qty_on_hand) as total_qty,
            SUM(bms.qty_on_hand * bms.avg_cost) as total_value
        FROM branch_material_stock bms
        JOIN item_master i ON bms.item_id = i.item_id
        WHERE (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
          AND bms.qty_on_hand > 0
        GROUP BY bms.item_id, i.item_desc
        ORDER BY i.item_desc
        """, nativeQuery = true)
    List<Object[]> findConsolidatedStockSummary(@Param("branchIds") List<String> branchIds, @Param("branchFilter") String branchFilter);

    /**
     * Branch-wise breakup for consolidated stock.
     */
    @Query(value = """
        SELECT
            bms.item_id,
            bms.branch_id,
            b.branch_name,
            SUM(bms.qty_on_hand) as qty,
            SUM(bms.qty_on_hand * bms.avg_cost) as value
        FROM branch_material_stock bms
        JOIN branch_master b ON bms.branch_id = b.branch_id
        WHERE (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
          AND bms.qty_on_hand > 0
        GROUP BY bms.item_id, bms.branch_id, b.branch_name
        ORDER BY bms.item_id, bms.branch_id
        """, nativeQuery = true)
    List<Object[]> findBranchWiseStockBreakup(@Param("branchIds") List<String> branchIds, @Param("branchFilter") String branchFilter);

    // ========================================================================
    // REPORT 3: STOCK LEDGER REPORT
    // ========================================================================
    /**
     * Complete stock movement history.
     * Indexes used: material_stock_ledger(branch_id, txn_date), (item_id)
     */
    @Query(value = """
        SELECT
            msl.ledger_id,
            msl.txn_date,
            msl.branch_id,
            b.branch_name,
            msl.item_id,
            i.item_desc,
            msl.location_id,
            l.location_name,
            msl.txn_type,
            msl.ref_id,
            msl.qty_in,
            msl.qty_out,
            msl.rate,
            msl.balance_qty,
            msl.created_on
        FROM material_stock_ledger msl
        JOIN branch_master b ON msl.branch_id = b.branch_id
        JOIN item_master i ON msl.item_id = i.item_id
        LEFT JOIN location_master l ON msl.location_id = l.location_id
        WHERE (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR msl.item_id = :itemId)
          AND (:fromDate IS NULL OR msl.txn_date >= :fromDate)
          AND (:toDate IS NULL OR msl.txn_date <= :toDate)
          AND (:txnType IS NULL OR msl.txn_type = :txnType)
          AND (:deptId IS NULL OR msl.dept_id = :deptId)
        ORDER BY msl.txn_date DESC, msl.ledger_id DESC
        """, nativeQuery = true)
    List<Object[]> findStockLedger(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("txnType") String txnType,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 4: STOCK AGING REPORT
    // ========================================================================
    /**
     * Stock aging based on GRN date (qty_remaining from grn_detail).
     * Aging buckets: 0-30, 31-60, 61-90, 90+
     * Indexes used: grn_header(grn_date), grn_detail(grn_id, item_id)
     */
    @Query(value = """
        SELECT
            gh.branch_id,
            b.branch_name,
            gd.item_id,
            i.item_desc,
            gd.location_id,
            l.location_name,
            CASE
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 30 THEN '0-30'
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 60 THEN '31-60'
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 90 THEN '61-90'
                ELSE '90+'
            END as aging_bucket,
            gd.qty_remaining as qty,
            gd.rate,
            (gd.qty_remaining * gd.rate) as value,
            DATEDIFF(CURDATE(), gh.grn_date) as age_days
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN item_master i ON gd.item_id = i.item_id
        LEFT JOIN location_master l ON gd.location_id = l.location_id
        WHERE gh.status = 'POSTED'
          AND gd.qty_remaining > 0
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR gd.item_id = :itemId)
        ORDER BY gh.branch_id, gd.item_id, gh.grn_date
        """, nativeQuery = true)
    List<Object[]> findStockAging(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId
    );

    /**
     * Aggregated aging summary by bucket.
     */
    @Query(value = """
        SELECT
            gh.branch_id,
            b.branch_name,
            gd.item_id,
            i.item_desc,
            CASE
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 30 THEN '0-30'
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 60 THEN '31-60'
                WHEN DATEDIFF(CURDATE(), gh.grn_date) <= 90 THEN '61-90'
                ELSE '90+'
            END as aging_bucket,
            SUM(gd.qty_remaining) as total_qty,
            SUM(gd.qty_remaining * gd.rate) as total_value
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN item_master i ON gd.item_id = i.item_id
        WHERE gh.status = 'POSTED'
          AND gd.qty_remaining > 0
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
        GROUP BY gh.branch_id, b.branch_name, gd.item_id, i.item_desc, aging_bucket
        ORDER BY gh.branch_id, gd.item_id, aging_bucket
        """, nativeQuery = true)
    List<Object[]> findStockAgingSummary(@Param("branchIds") List<String> branchIds, @Param("branchFilter") String branchFilter);

    // ========================================================================
    // REPORT 5: FIFO CONSUMPTION REPORT
    // ========================================================================
    /**
     * FIFO consumption details showing which GRN batches were consumed.
     * Indexes used: issue_fifo_consumption(issue_id, item_id), grn_header(grn_id)
     */
    @Query(value = """
        SELECT
            ifc.issue_id,
            ih.issue_date,
            ih.branch_id,
            b.branch_name,
            ifc.item_id,
            i.item_desc,
            id.qty_issued,
            id.rate as weighted_avg_rate,
            ifc.grn_id,
            gh.grn_date,
            ifc.qty_consumed,
            ifc.rate as grn_rate,
            (ifc.qty_consumed * ifc.rate) as consumption_value
        FROM issue_fifo_consumption ifc
        JOIN issue_header ih ON ifc.issue_id = ih.issue_id
        JOIN issue_detail id ON ifc.issue_id = id.issue_id AND ifc.item_id = id.item_id
        JOIN grn_header gh ON ifc.grn_id = gh.grn_id
        JOIN branch_master b ON ih.branch_id = b.branch_id
        JOIN item_master i ON ifc.item_id = i.item_id
        WHERE ih.status = 'POSTED'
          AND (:branchFilter IS NULL OR ih.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR ifc.item_id = :itemId)
          AND (:fromDate IS NULL OR ih.issue_date >= :fromDate)
          AND (:toDate IS NULL OR ih.issue_date <= :toDate)
        ORDER BY ih.issue_date DESC, ifc.issue_id, ifc.item_id, gh.grn_date
        """, nativeQuery = true)
    List<Object[]> findFifoConsumption(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ========================================================================
    // REPORT 6: GRN SUMMARY REPORT
    // ========================================================================
    /**
     * GRN summary with totals.
     * Indexes used: grn_header(branch_id, grn_date), (supp_id)
     */
    @Query(value = """
        SELECT
            gh.grn_id,
            gh.branch_id,
            b.branch_name,
            gh.supp_id,
            s.supp_name,
            si.invoice_no,
            gh.grn_date,
            SUM(gd.qty_received) as total_qty,
            SUM(gd.gross_amount) as total_value,
            gh.status,
            gh.created_by,
            gh.approved_by
        FROM grn_header gh
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        LEFT JOIN supplier_invoice si ON gh.invoice_id = si.invoice_id
        LEFT JOIN grn_detail gd ON gh.grn_id = gd.grn_id
        WHERE (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:status IS NULL OR gh.status = :status)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        GROUP BY gh.grn_id, gh.branch_id, b.branch_name, gh.supp_id, s.supp_name,
                 si.invoice_no, gh.grn_date, gh.status, gh.created_by, gh.approved_by
        ORDER BY gh.grn_date DESC, gh.grn_id DESC
        """, nativeQuery = true)
    List<Object[]> findGrnSummary(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 7: GRN VS INVOICE COMPARISON
    // ========================================================================
    /**
     * Compare GRN amount vs Invoice amount.
     * Indexes used: grn_header(invoice_id), supplier_invoice(invoice_id)
     */
    @Query(value = """
        SELECT
            gh.grn_id,
            gh.branch_id,
            b.branch_name,
            gh.supp_id,
            s.supp_name,
            si.invoice_no,
            si.invoice_date,
            si.net_amount as invoice_amount,
            (SELECT SUM(gd.gross_amount) FROM grn_detail gd WHERE gd.grn_id = gh.grn_id) as grn_amount,
            (si.net_amount - (SELECT SUM(gd.gross_amount) FROM grn_detail gd WHERE gd.grn_id = gh.grn_id)) as difference,
            CASE
                WHEN si.net_amount = 0 THEN 0
                ELSE ((si.net_amount - (SELECT SUM(gd.gross_amount) FROM grn_detail gd WHERE gd.grn_id = gh.grn_id)) / si.net_amount * 100)
            END as difference_percent,
            gh.status as approval_status,
            gh.approved_by
        FROM grn_header gh
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        LEFT JOIN supplier_invoice si ON gh.invoice_id = si.invoice_id
        WHERE (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        ORDER BY gh.grn_date DESC
        """, nativeQuery = true)
    List<Object[]> findGrnVsInvoice(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 8: PRICE VARIANCE REPORT
    // ========================================================================
    /**
     * Price variance - compare GRN rate vs last GRN rate or item master price.
     * This requires joining with previous GRN to get reference price.
     */
    @Query(value = """
        SELECT
            gd.grn_id,
            gh.grn_date,
            gh.branch_id,
            b.branch_name,
            gd.item_id,
            i.item_desc,
            gh.supp_id,
            s.supp_name,
            i.cost_price as reference_price,
            gd.rate as entered_price,
            (gd.rate - i.cost_price) as variance_amount,
            CASE
                WHEN i.cost_price = 0 THEN 0
                ELSE ((gd.rate - i.cost_price) / i.cost_price * 100)
            END as variance_percent,
            gh.status,
            gh.approved_by,
            gh.approved_at
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN item_master i ON gd.item_id = i.item_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        WHERE (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR gd.item_id = :itemId)
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:minVariance IS NULL OR ABS((gd.rate - i.cost_price) / NULLIF(i.cost_price, 0) * 100) >= :minVariance)
        ORDER BY gh.grn_date DESC, variance_percent DESC
        """, nativeQuery = true)
    List<Object[]> findPriceVariance(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("suppId") String suppId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minVariance") BigDecimal minVariance
    );

    // ========================================================================
    // REPORT 9: PO VS GRN REPORT
    // ========================================================================
    /**
     * Purchase Order fulfillment status.
     * Indexes used: po_header(branch_id, status), po_detail(po_id, item_id)
     */
    @Query(value = """
        SELECT
            ph.po_id,
            ph.po_date,
            ph.branch_id,
            b.branch_name,
            ph.supp_id,
            s.supp_name,
            pd.item_id,
            i.item_desc,
            pd.qty_ordered,
            pd.qty_received,
            (pd.qty_ordered - pd.qty_received) as pending_qty,
            pd.rate as po_rate,
            ph.status,
            CASE
                WHEN pd.qty_ordered = 0 THEN 100
                ELSE (pd.qty_received / pd.qty_ordered * 100)
            END as fulfillment_percent
        FROM po_detail pd
        JOIN po_header ph ON pd.po_id = ph.po_id
        JOIN branch_master b ON ph.branch_id = b.branch_id
        JOIN supplier_master s ON ph.supp_id = s.supp_id
        JOIN item_master i ON pd.item_id = i.item_id
        WHERE (:branchFilter IS NULL OR ph.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR ph.supp_id = :suppId)
          AND (:itemId IS NULL OR pd.item_id = :itemId)
          AND (:fromDate IS NULL OR ph.po_date >= :fromDate)
          AND (:toDate IS NULL OR ph.po_date <= :toDate)
          AND (:status IS NULL OR ph.status = :status)
        ORDER BY ph.po_date DESC, ph.po_id
        """, nativeQuery = true)
    List<Object[]> findPoVsGrn(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // ========================================================================
    // REPORT 10: INTER-BRANCH TRANSFER REPORT
    // ========================================================================
    /**
     * Stock transfers between branches.
     * Indexes used: stock_transfer_header(from_branch, to_branch, status)
     */
    @Query(value = """
        SELECT
            dth.dept_transfer_id,
            dth.transfer_date,
            dth.from_branch_id,
            b.branch_name,
            dth.from_dept_id,
            fd.dept_name as from_dept_name,
            dth.to_dept_id,
            td.dept_name as to_dept_name,
            dtd.item_id,
            i.item_desc,
            dtd.qty_transferred,
            dtd.rate,
            (COALESCE(dtd.qty_transferred, 0) * COALESCE(dtd.rate, 0)) as value,
            dth.status,
            dth.created_by
        FROM dept_transfer_header dth
        JOIN branch_master b ON dth.from_branch_id = b.branch_id
        JOIN department_master fd ON dth.from_dept_id = fd.dept_id
        JOIN department_master td ON dth.to_dept_id = td.dept_id
        LEFT JOIN dept_transfer_detail dtd ON dtd.dept_transfer_id = dth.dept_transfer_id
        LEFT JOIN item_master i ON dtd.item_id = i.item_id
        WHERE (:branchFilter IS NULL OR dth.from_branch_id IN (:branchIds) OR dth.to_branch_id IN (:branchIds))
          AND (:itemId IS NULL OR dtd.item_id = :itemId)
          AND (:fromDate IS NULL OR dth.transfer_date >= :fromDate)
          AND (:toDate IS NULL OR dth.transfer_date <= :toDate)
          AND (:status IS NULL OR dth.status = :status)
          AND (:deptId IS NULL OR dth.from_dept_id = :deptId OR dth.to_dept_id = :deptId)
        ORDER BY dth.transfer_date DESC, dth.dept_transfer_id
        """, nativeQuery = true)
    List<Object[]> findInterBranchTransfers(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 11: ISSUE TO PRODUCTION REPORT (ENHANCED with dept/type)
    // ========================================================================
    @Query(value = """
        SELECT
            ih.issue_id,
            ih.issue_date,
            ih.branch_id,
            b.branch_name,
            id.item_id,
            i.item_desc,
            id.qty_issued,
            id.rate as fifo_rate,
            (id.qty_issued * id.rate) as total_value,
            ih.issued_to,
            id.location_id,
            l.location_name,
            ih.status,
            ih.dept_id,
            dm.dept_name,
            ih.issue_type,
            ih.supp_id,
            s.supp_name
        FROM issue_detail id
        JOIN issue_header ih ON id.issue_id = ih.issue_id
        JOIN branch_master b ON ih.branch_id = b.branch_id
        JOIN item_master i ON id.item_id = i.item_id
        LEFT JOIN location_master l ON id.location_id = l.location_id
        LEFT JOIN department_master dm ON ih.dept_id = dm.dept_id
        LEFT JOIN supplier_master s ON ih.supp_id = s.supp_id
        WHERE (:branchFilter IS NULL OR ih.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR id.item_id = :itemId)
          AND (:fromDate IS NULL OR ih.issue_date >= :fromDate)
          AND (:toDate IS NULL OR ih.issue_date <= :toDate)
          AND (:status IS NULL OR ih.status = :status)
          AND (:deptId IS NULL OR ih.dept_id = :deptId)
        ORDER BY ih.issue_date DESC, ih.issue_id
        """, nativeQuery = true)
    List<Object[]> findIssueToProduction(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 12: NON-MOVING / SLOW-MOVING STOCK
    // ========================================================================
    /**
     * Stock with no movement in last N days.
     * Uses stock ledger to find last movement date.
     */
    @Query(value = """
        SELECT
            bms.branch_id,
            b.branch_name,
            bms.item_id,
            i.item_desc,
            bms.location_id,
            l.location_name,
            (SELECT MAX(msl.txn_date)
             FROM material_stock_ledger msl
             WHERE msl.branch_id = bms.branch_id
               AND msl.item_id = bms.item_id
               AND msl.location_id = bms.location_id) as last_movement_date,
            DATEDIFF(CURDATE(),
                     (SELECT MAX(msl.txn_date)
                      FROM material_stock_ledger msl
                      WHERE msl.branch_id = bms.branch_id
                        AND msl.item_id = bms.item_id
                        AND msl.location_id = bms.location_id)) as days_since_last_movement,
            bms.qty_on_hand,
            bms.avg_cost,
            (bms.qty_on_hand * bms.avg_cost) as value,
            CASE
                WHEN DATEDIFF(CURDATE(),
                              (SELECT MAX(msl.txn_date)
                               FROM material_stock_ledger msl
                               WHERE msl.branch_id = bms.branch_id
                                 AND msl.item_id = bms.item_id
                                 AND msl.location_id = bms.location_id)) > :nonMovingDays THEN 'NON_MOVING'
                ELSE 'SLOW_MOVING'
            END as movement_category
        FROM branch_material_stock bms
        JOIN branch_master b ON bms.branch_id = b.branch_id
        JOIN item_master i ON bms.item_id = i.item_id
        LEFT JOIN location_master l ON bms.location_id = l.location_id
        WHERE bms.qty_on_hand > 0
          AND (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR bms.item_id = :itemId)
        HAVING days_since_last_movement >= :minDays
        ORDER BY days_since_last_movement DESC, value DESC
        """, nativeQuery = true)
    List<Object[]> findNonMovingStock(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("minDays") Integer minDays,
            @Param("nonMovingDays") Integer nonMovingDays
    );

    // ========================================================================
    // REPORT 13a: SUPPLIER REPORT - SUMMARY
    // ========================================================================
    @Query(value = """
        SELECT
            s.supp_id,
            s.supp_name,
            gd.item_id,
            i.item_desc,
            SUM(gd.qty_received) as total_qty_received,
            SUM(gd.net_amount) as total_value,
            CASE WHEN SUM(gd.qty_received) = 0 THEN 0
                 ELSE SUM(gd.net_amount) / SUM(gd.qty_received)
            END as avg_rate,
            COUNT(DISTINCT gh.grn_id) as grn_count
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        JOIN item_master i ON gd.item_id = i.item_id
        WHERE gh.status = 'POSTED'
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:itemId IS NULL OR gd.item_id = :itemId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        GROUP BY s.supp_id, s.supp_name, gd.item_id, i.item_desc
        ORDER BY s.supp_name, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findSupplierReportSummary(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 13b: SUPPLIER REPORT - DETAIL
    // ========================================================================
    @Query(value = """
        SELECT
            s.supp_id,
            s.supp_name,
            gh.grn_id,
            gh.grn_date,
            gh.pv_id,
            pvh.pv_date,
            gd.item_id,
            i.item_desc,
            gd.qty_received,
            gd.rate,
            gd.net_amount,
            gh.branch_id,
            b.branch_name,
            si.invoice_no,
            gh.dept_id,
            dm.dept_name
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        JOIN item_master i ON gd.item_id = i.item_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        LEFT JOIN purchase_voucher_header pvh ON gh.pv_id = pvh.pv_id
        LEFT JOIN supplier_invoice si ON gh.invoice_id = si.invoice_id
        LEFT JOIN department_master dm ON gh.dept_id = dm.dept_id
        WHERE gh.status = 'POSTED'
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:itemId IS NULL OR gd.item_id = :itemId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        ORDER BY s.supp_name, gh.grn_date DESC
        """, nativeQuery = true)
    List<Object[]> findSupplierReportDetail(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 14: AUDIT & EXCEPTION REPORT
    // ========================================================================
    /**
     * GRNs without PO reference.
     */
    @Query(value = """
        SELECT
            'GRN_WITHOUT_PV' as exception_type,
            gh.branch_id,
            b.branch_name,
            gh.grn_id as ref_id,
            'GRN' as ref_type,
            gh.grn_date as txn_date,
            CONCAT('GRN without Purchase Voucher from supplier: ', s.supp_name) as description,
            (SELECT SUM(gd.gross_amount) FROM grn_detail gd WHERE gd.grn_id = gh.grn_id) as amount,
            NULL as variance_percent,
            gh.approved_by,
            gh.approved_at,
            'MEDIUM' as severity
        FROM grn_header gh
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        WHERE gh.pv_id IS NULL
          AND gh.status = 'POSTED'
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        """, nativeQuery = true)
    List<Object[]> findGrnsWithoutPo(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    /**
     * High price variance approvals.
     */
    @Query(value = """
        SELECT
            'HIGH_PRICE_VARIANCE' as exception_type,
            gh.branch_id,
            b.branch_name,
            gh.grn_id as ref_id,
            'GRN' as ref_type,
            gh.grn_date as txn_date,
            CONCAT('High price variance for item: ', i.item_desc) as description,
            gd.gross_amount as amount,
            CASE
                WHEN i.cost_price = 0 THEN 0
                ELSE ((gd.rate - i.cost_price) / i.cost_price * 100)
            END as variance_percent,
            gh.approved_by,
            gh.approved_at,
            CASE
                WHEN ABS((gd.rate - i.cost_price) / NULLIF(i.cost_price, 0) * 100) > 20 THEN 'CRITICAL'
                WHEN ABS((gd.rate - i.cost_price) / NULLIF(i.cost_price, 0) * 100) > 10 THEN 'HIGH'
                ELSE 'MEDIUM'
            END as severity
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN item_master i ON gd.item_id = i.item_id
        WHERE gh.status = 'POSTED'
          AND gh.approved_by IS NOT NULL
          AND ABS((gd.rate - i.cost_price) / NULLIF(i.cost_price, 0) * 100) > :minVariance
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        ORDER BY variance_percent DESC
        """, nativeQuery = true)
    List<Object[]> findHighVarianceApprovals(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minVariance") BigDecimal minVariance,
            @Param("deptId") Integer deptId
    );

    /**
     * Transfer shortages (sent != received).
     */
    @Query(value = """
        SELECT
            'TRANSFER_SHORTAGE' as exception_type,
            sth.from_branch as branch_id,
            fb.branch_name,
            sth.transfer_id as ref_id,
            'TRANSFER' as ref_type,
            sth.transfer_date as txn_date,
            CONCAT('Transfer shortage from ', fb.branch_name, ' to ', tb.branch_name,
                   ' for item: ', i.item_desc) as description,
            ((std.qty_sent - COALESCE(std.qty_received, 0)) * std.rate) as amount,
            CASE
                WHEN std.qty_sent = 0 THEN 0
                ELSE ((std.qty_sent - COALESCE(std.qty_received, 0)) / std.qty_sent * 100)
            END as variance_percent,
            sth.approved_by,
            sth.approved_at,
            CASE
                WHEN ((std.qty_sent - COALESCE(std.qty_received, 0)) / NULLIF(std.qty_sent, 0) * 100) > 10 THEN 'HIGH'
                ELSE 'MEDIUM'
            END as severity
        FROM stock_transfer_detail std
        JOIN stock_transfer_header sth ON std.transfer_id = sth.transfer_id
        JOIN branch_master fb ON sth.from_branch = fb.branch_id
        JOIN branch_master tb ON sth.to_branch = tb.branch_id
        JOIN item_master i ON std.item_id = i.item_id
        WHERE sth.status = 'RECEIVED'
          AND std.qty_sent > COALESCE(std.qty_received, 0)
          AND (:branchFilter IS NULL OR sth.from_branch IN (:branchIds) OR sth.to_branch IN (:branchIds))
          AND (:fromDate IS NULL OR sth.transfer_date >= :fromDate)
          AND (:toDate IS NULL OR sth.transfer_date <= :toDate)
        ORDER BY amount DESC
        """, nativeQuery = true)
    List<Object[]> findTransferShortages(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ========================================================================
    // REPORT 15: STOCK STATEMENT (MOVEMENT SUMMARY)
    // ========================================================================
    /**
     * Stock statement showing opening balance, inward, outward for a date range.
     * Opening = balance_qty from last ledger entry before fromDate.
     * Closing = opening + inward - outward.
     */
    @Query(value = """
        SELECT
            msl.branch_id,
            b.branch_name,
            msl.item_id,
            i.item_desc,
            msl.location_id,
            l.location_name,
            COALESCE((
                SELECT sub.balance_qty FROM material_stock_ledger sub
                WHERE sub.branch_id = msl.branch_id AND sub.item_id = msl.item_id
                  AND sub.location_id = msl.location_id AND sub.txn_date < :fromDate
                  AND (:deptId IS NULL OR sub.dept_id = :deptId)
                ORDER BY sub.ledger_id DESC LIMIT 1
            ), 0) as opening_qty,
            SUM(msl.qty_in) as inward_qty,
            SUM(msl.qty_out) as outward_qty
        FROM material_stock_ledger msl
        JOIN branch_master b ON msl.branch_id = b.branch_id
        JOIN item_master i ON msl.item_id = i.item_id
        LEFT JOIN location_master l ON msl.location_id = l.location_id
        WHERE msl.txn_date >= :fromDate AND msl.txn_date <= :toDate
          AND (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR msl.item_id = :itemId)
          AND (:locationId IS NULL OR msl.location_id = :locationId)
          AND (:deptId IS NULL OR msl.dept_id = :deptId)
        GROUP BY msl.branch_id, b.branch_name, msl.item_id, i.item_desc,
                 msl.location_id, l.location_name
        ORDER BY msl.branch_id, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findStockStatement(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    /**
     * Items with stock before a date but no movement in the date range.
     * Used to include items with opening balance but zero movement.
     */
    @Query(value = """
        SELECT
            msl.branch_id,
            b.branch_name,
            msl.item_id,
            i.item_desc,
            msl.location_id,
            l.location_name,
            msl.balance_qty as opening_qty
        FROM material_stock_ledger msl
        JOIN branch_master b ON msl.branch_id = b.branch_id
        JOIN item_master i ON msl.item_id = i.item_id
        LEFT JOIN location_master l ON msl.location_id = l.location_id
        WHERE msl.ledger_id = (
            SELECT MAX(sub.ledger_id) FROM material_stock_ledger sub
            WHERE sub.branch_id = msl.branch_id AND sub.item_id = msl.item_id
              AND sub.location_id = msl.location_id AND sub.txn_date < :fromDate
              AND (:deptId IS NULL OR sub.dept_id = :deptId)
        )
          AND msl.balance_qty > 0
          AND (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND (:itemId IS NULL OR msl.item_id = :itemId)
          AND (:locationId IS NULL OR msl.location_id = :locationId)
          AND (:deptId IS NULL OR msl.dept_id = :deptId)
          AND NOT EXISTS (
              SELECT 1 FROM material_stock_ledger inrange
              WHERE inrange.branch_id = msl.branch_id AND inrange.item_id = msl.item_id
                AND inrange.location_id = msl.location_id
                AND inrange.txn_date >= :fromDate AND inrange.txn_date <= :toDate
                AND (:deptId IS NULL OR inrange.dept_id = :deptId)
          )
        ORDER BY msl.branch_id, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findItemsWithOpeningOnly(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 16: GRN DETAIL REPORT
    // ========================================================================
    @Query(value = """
        SELECT
            gh.grn_id,
            gh.grn_date,
            gh.branch_id,
            b.branch_name,
            gh.supp_id,
            s.supp_name,
            gd.item_id,
            i.item_desc,
            gd.unit_id,
            gd.qty_received,
            gd.rate,
            gd.gross_amount,
            gd.gst_perc,
            gd.gst_amount,
            gd.discount_perc,
            gd.discount_amount,
            gd.net_amount,
            gd.location_id,
            l.location_name,
            si.invoice_no,
            gh.status,
            gh.dept_id,
            dm.dept_name
        FROM grn_detail gd
        JOIN grn_header gh ON gd.grn_id = gh.grn_id
        JOIN branch_master b ON gh.branch_id = b.branch_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        JOIN item_master i ON gd.item_id = i.item_id
        LEFT JOIN location_master l ON gd.location_id = l.location_id
        LEFT JOIN supplier_invoice si ON gh.invoice_id = si.invoice_id
        LEFT JOIN department_master dm ON gh.dept_id = dm.dept_id
        WHERE gh.status = 'POSTED'
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR gh.supp_id = :suppId)
          AND (:itemId IS NULL OR gd.item_id = :itemId)
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        ORDER BY gh.grn_date DESC, gh.grn_id, gd.item_id
        """, nativeQuery = true)
    List<Object[]> findGrnDetail(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT: RECEIPT NOTE ITEM-WISE
    // ========================================================================
    @Query(value = """
        SELECT
            pvh.pv_date,
            pvh.voucher_number,
            b.branch_name,
            sm.supp_name,
            po.voucher_number AS po_no,
            po.pv_date AS po_date,
            pvh.supplier_invoice_no,
            pvh.supplier_invoice_date,
            pvd.item_id,
            im.item_desc,
            um.unit_desc,
            pvd.qty,
            pvd.rate,
            pvd.gross_amount,
            pvd.gst_perc,
            pvd.gst_amount,
            pvd.net_amount,
            pvh.narration
        FROM purchase_voucher_header pvh
        JOIN purchase_voucher_detail pvd ON pvh.pv_id = pvd.pv_id
        JOIN branch_master b ON pvh.branch_id = b.branch_id
        JOIN supplier_master sm ON pvh.supp_id = sm.supp_id
        JOIN item_master im ON pvd.item_id = im.item_id
        JOIN unit_master um ON pvd.unit_id = um.unit_id
        LEFT JOIN purchase_voucher_header po ON pvh.linked_po_id = CAST(po.pv_id AS CHAR)
        WHERE pvh.voucher_category = 'Receipt Note'
          AND pvh.status = 'POSTED'
          AND (:branchId IS NULL OR pvh.branch_id = :branchId)
          AND (:suppId IS NULL OR pvh.supp_id = :suppId)
          AND (:itemId IS NULL OR pvd.item_id = :itemId)
          AND (:fromDate IS NULL OR pvh.pv_date >= :fromDate)
          AND (:toDate IS NULL OR pvh.pv_date <= :toDate)
        ORDER BY pvh.pv_date, pvh.pv_id, pvd.item_id
        """, nativeQuery = true)
    List<Object[]> findReceiptNoteItems(
            @Param("branchId") String branchId,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ========================================================================
    // REPORT: PURCHASE ORDER ITEM-WISE
    // ========================================================================
    @Query(value = """
        SELECT
            pvh.pv_date,
            sm.supp_name,
            COALESCE(l.location_name, b.branch_name),
            pvh.voucher_number,
            pvh.effective_date,
            pvd.item_id,
            im.item_desc,
            um.unit_desc,
            pvd.ordered_qty,
            pvd.rate,
            pvd.gross_amount,
            pvd.gst_perc,
            pvd.gst_amount,
            pvd.net_amount,
            pvd.line_narration
        FROM purchase_voucher_header pvh
        JOIN purchase_voucher_detail pvd ON pvh.pv_id = pvd.pv_id
        JOIN branch_master b ON pvh.branch_id = b.branch_id
        JOIN supplier_master sm ON pvh.supp_id = sm.supp_id
        JOIN item_master im ON pvd.item_id = im.item_id
        JOIN unit_master um ON pvd.unit_id = um.unit_id
        LEFT JOIN location_master l ON pvd.location_id = l.location_id
        WHERE pvh.voucher_category = 'Purchase Order'
          AND (:branchId IS NULL OR pvh.branch_id = :branchId)
          AND (:suppId IS NULL OR pvh.supp_id = :suppId)
          AND (:itemId IS NULL OR pvd.item_id = :itemId)
          AND (:fromDate IS NULL OR pvh.pv_date >= :fromDate)
          AND (:toDate IS NULL OR pvh.pv_date <= :toDate)
        ORDER BY pvh.pv_date, pvh.pv_id, pvd.item_id
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findPurchaseOrderItems(
            @Param("branchId") String branchId,
            @Param("suppId") String suppId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // ========================================================================
    // REPORT 17: ITEM REPORT - SUMMARY
    // ========================================================================
    @Query(value = """
        SELECT
            i.item_id,
            i.item_desc,
            i.group_id,
            i.sub_group_id,
            COALESCE(stock.total_qty, 0) as current_stock_qty,
            COALESCE(stock.total_value, 0) as current_stock_value,
            COALESCE(grns.total_qty, 0) as total_grn_qty,
            COALESCE(grns.total_value, 0) as total_grn_value,
            COALESCE(issues.total_qty, 0) as total_issue_qty,
            COALESCE(issues.total_value, 0) as total_issue_value,
            COALESCE(grns.supp_count, 0) as supplier_count,
            grns.last_grn_date,
            issues.last_issue_date
        FROM item_master i
        LEFT JOIN (
            SELECT bms.item_id,
                   SUM(bms.qty_on_hand) as total_qty,
                   SUM(bms.qty_on_hand * bms.avg_cost) as total_value
            FROM branch_material_stock bms
            WHERE bms.qty_on_hand > 0
              AND (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
            GROUP BY bms.item_id
        ) stock ON i.item_id = stock.item_id
        LEFT JOIN (
            SELECT gd.item_id,
                   SUM(gd.qty_received) as total_qty,
                   SUM(gd.net_amount) as total_value,
                   COUNT(DISTINCT gh.supp_id) as supp_count,
                   MAX(gh.grn_date) as last_grn_date
            FROM grn_detail gd
            JOIN grn_header gh ON gd.grn_id = gh.grn_id
            WHERE gh.status = 'POSTED'
              AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
              AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
              AND (:toDate IS NULL OR gh.grn_date <= :toDate)
              AND (:deptId IS NULL OR gh.dept_id = :deptId)
            GROUP BY gd.item_id
        ) grns ON i.item_id = grns.item_id
        LEFT JOIN (
            SELECT id.item_id,
                   SUM(id.qty_issued) as total_qty,
                   SUM(id.qty_issued * id.rate) as total_value,
                   MAX(ih.issue_date) as last_issue_date
            FROM issue_detail id
            JOIN issue_header ih ON id.issue_id = ih.issue_id
            WHERE ih.status = 'POSTED'
              AND (:branchFilter IS NULL OR ih.branch_id IN (:branchIds))
              AND (:fromDate IS NULL OR ih.issue_date >= :fromDate)
              AND (:toDate IS NULL OR ih.issue_date <= :toDate)
              AND (:deptId IS NULL OR ih.dept_id = :deptId)
            GROUP BY id.item_id
        ) issues ON i.item_id = issues.item_id
        WHERE (:itemId IS NULL OR i.item_id = :itemId)
          AND (stock.total_qty IS NOT NULL OR grns.total_qty IS NOT NULL OR issues.total_qty IS NOT NULL)
        ORDER BY i.item_desc
        """, nativeQuery = true)
    List<Object[]> findItemReportSummary(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 18: ITEM REPORT - DETAIL (all transactions for an item)
    // ========================================================================
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            msl.txn_type,
            msl.ref_id,
            msl.txn_date,
            msl.branch_id,
            b.branch_name,
            msl.qty_in,
            msl.qty_out,
            msl.rate,
            CASE WHEN msl.qty_in > 0 THEN msl.qty_in * msl.rate
                 ELSE msl.qty_out * msl.rate
            END as value,
            CASE
                WHEN msl.txn_type = 'GRN' THEN (SELECT s.supp_name FROM grn_header gh
                    JOIN supplier_master s ON gh.supp_id = s.supp_id WHERE gh.grn_id = msl.ref_id)
                WHEN msl.txn_type = 'ISSUE' THEN (SELECT COALESCE(dm.dept_name, ih.issued_to) FROM issue_header ih
                    LEFT JOIN department_master dm ON ih.dept_id = dm.dept_id WHERE ih.issue_id = msl.ref_id)
                WHEN msl.txn_type IN ('TRANSFER_IN', 'TRANSFER_OUT') THEN (SELECT CONCAT(fd.dept_name, ' -> ', td.dept_name)
                    FROM dept_transfer_header dth
                    JOIN department_master fd ON dth.from_dept_id = fd.dept_id
                    JOIN department_master td ON dth.to_dept_id = td.dept_id
                    WHERE dth.dept_transfer_id = msl.ref_id)
                ELSE NULL
            END as counterparty,
            CASE
                WHEN msl.txn_type = 'GRN' THEN (SELECT si.invoice_no FROM grn_header gh
                    LEFT JOIN supplier_invoice si ON gh.invoice_id = si.invoice_id WHERE gh.grn_id = msl.ref_id)
                ELSE NULL
            END as ref_no
        FROM material_stock_ledger msl
        JOIN item_master i ON msl.item_id = i.item_id
        JOIN branch_master b ON msl.branch_id = b.branch_id
        WHERE msl.item_id = :itemId
          AND (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND (:fromDate IS NULL OR msl.txn_date >= :fromDate)
          AND (:toDate IS NULL OR msl.txn_date <= :toDate)
          AND (:deptId IS NULL OR msl.dept_id = :deptId)
        ORDER BY msl.txn_date DESC, msl.ledger_id DESC
        """, nativeQuery = true)
    List<Object[]> findItemReportDetail(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 19: BRANCH REPORT
    // ========================================================================
    @Query(value = """
        SELECT
            b.branch_id,
            b.branch_name,
            COALESCE(stock.stock_value, 0) as current_stock_value,
            COALESCE(stock.stock_items, 0) as current_stock_items,
            COALESCE(grns.grn_count, 0) as grn_count,
            COALESCE(grns.grn_value, 0) as grn_value,
            COALESCE(issues.issue_count, 0) as issue_count,
            COALESCE(issues.issue_value, 0) as issue_value,
            COALESCE(tin.transfer_in_count, 0) as transfer_in_count,
            COALESCE(tin.transfer_in_value, 0) as transfer_in_value,
            COALESCE(tout.transfer_out_count, 0) as transfer_out_count,
            COALESCE(tout.transfer_out_value, 0) as transfer_out_value
        FROM branch_master b
        LEFT JOIN (
            SELECT bms.branch_id,
                   SUM(bms.qty_on_hand * bms.avg_cost) as stock_value,
                   COUNT(DISTINCT bms.item_id) as stock_items
            FROM branch_material_stock bms
            WHERE bms.qty_on_hand > 0
            GROUP BY bms.branch_id
        ) stock ON b.branch_id = stock.branch_id
        LEFT JOIN (
            SELECT gh.branch_id,
                   COUNT(DISTINCT gh.grn_id) as grn_count,
                   SUM(gd.net_amount) as grn_value
            FROM grn_header gh
            JOIN grn_detail gd ON gh.grn_id = gd.grn_id
            WHERE gh.status = 'POSTED'
              AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
              AND (:toDate IS NULL OR gh.grn_date <= :toDate)
              AND (:deptId IS NULL OR gh.dept_id = :deptId)
            GROUP BY gh.branch_id
        ) grns ON b.branch_id = grns.branch_id
        LEFT JOIN (
            SELECT ih.branch_id,
                   COUNT(DISTINCT ih.issue_id) as issue_count,
                   SUM(id.qty_issued * id.rate) as issue_value
            FROM issue_header ih
            JOIN issue_detail id ON ih.issue_id = id.issue_id
            WHERE ih.status = 'POSTED'
              AND (:fromDate IS NULL OR ih.issue_date >= :fromDate)
              AND (:toDate IS NULL OR ih.issue_date <= :toDate)
              AND (:deptId IS NULL OR ih.dept_id = :deptId)
            GROUP BY ih.branch_id
        ) issues ON b.branch_id = issues.branch_id
        LEFT JOIN (
            SELECT msl.branch_id,
                   COUNT(DISTINCT msl.ref_id) as transfer_in_count,
                   SUM(msl.qty_in * msl.rate) as transfer_in_value
            FROM material_stock_ledger msl
            WHERE msl.txn_type = 'TRANSFER_IN'
              AND (:fromDate IS NULL OR msl.txn_date >= :fromDate)
              AND (:toDate IS NULL OR msl.txn_date <= :toDate)
              AND (:deptId IS NULL OR msl.dept_id = :deptId)
            GROUP BY msl.branch_id
        ) tin ON b.branch_id = tin.branch_id
        LEFT JOIN (
            SELECT msl.branch_id,
                   COUNT(DISTINCT msl.ref_id) as transfer_out_count,
                   SUM(msl.qty_out * msl.rate) as transfer_out_value
            FROM material_stock_ledger msl
            WHERE msl.txn_type = 'TRANSFER_OUT'
              AND (:fromDate IS NULL OR msl.txn_date >= :fromDate)
              AND (:toDate IS NULL OR msl.txn_date <= :toDate)
              AND (:deptId IS NULL OR msl.dept_id = :deptId)
            GROUP BY msl.branch_id
        ) tout ON b.branch_id = tout.branch_id
        WHERE (:branchFilter IS NULL OR b.branch_id IN (:branchIds))
          AND (stock.stock_value IS NOT NULL OR grns.grn_count IS NOT NULL
               OR issues.issue_count IS NOT NULL OR tin.transfer_in_count IS NOT NULL
               OR tout.transfer_out_count IS NOT NULL)
        ORDER BY b.branch_name
        """, nativeQuery = true)
    List<Object[]> findBranchReport(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    /** Top items by stock value for a branch. */
    @Query(value = """
        SELECT
            bms.branch_id,
            bms.item_id,
            i.item_desc,
            SUM(bms.qty_on_hand) as stock_qty,
            SUM(bms.qty_on_hand * bms.avg_cost) as stock_value
        FROM branch_material_stock bms
        JOIN item_master i ON bms.item_id = i.item_id
        WHERE bms.qty_on_hand > 0
          AND (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))
        GROUP BY bms.branch_id, bms.item_id, i.item_desc
        ORDER BY bms.branch_id, stock_value DESC
        """, nativeQuery = true)
    List<Object[]> findBranchTopItems(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter
    );

    /** Top suppliers by GRN value for a branch. */
    @Query(value = """
        SELECT
            gh.branch_id,
            s.supp_id,
            s.supp_name,
            COUNT(DISTINCT gh.grn_id) as grn_count,
            SUM(gd.net_amount) as total_value
        FROM grn_header gh
        JOIN grn_detail gd ON gh.grn_id = gd.grn_id
        JOIN supplier_master s ON gh.supp_id = s.supp_id
        WHERE gh.status = 'POSTED'
          AND (:branchFilter IS NULL OR gh.branch_id IN (:branchIds))
          AND (:fromDate IS NULL OR gh.grn_date >= :fromDate)
          AND (:toDate IS NULL OR gh.grn_date <= :toDate)
          AND (:deptId IS NULL OR gh.dept_id = :deptId)
        GROUP BY gh.branch_id, s.supp_id, s.supp_name
        ORDER BY gh.branch_id, total_value DESC
        """, nativeQuery = true)
    List<Object[]> findBranchTopSuppliers(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deptId") Integer deptId
    );

    // ========================================================================
    // REPORT 20: PV VS GRN
    // Base : Purchase Order type PVs  (one row per PO × item)
    // Received qty / freight: SUM across all linked Receipt Note PVs
    // Computed: basic_amount = received_qty × po_rate
    //           gst_amt      = gst_perc/100  × basic_amount
    //           total        = basic_amount + gst_amt + freight + gst_on_freight
    // ========================================================================
    @Query(value = """
        SELECT
            po.pv_date                                                  AS date,
            s.supp_name                                                 AS supplier_name,
            l.location_name                                             AS location,
            po.voucher_number                                           AS po_no,
            po.pv_date                                                  AS po_date,
            po_d.item_id                                                AS item_code,
            i.item_desc                                                 AS item_description,
            u.unit_desc                                                 AS unit,
            po_d.ordered_qty                                            AS po_qty,
            po_d.rate                                                   AS po_basic_price,
            po_d.gross_amount                                           AS po_basic_amount,
            po_d.net_amount                                             AS po_total_amount,
            IFNULL(SUM(rn_d.qty), 0)                                   AS received_qty,
            po_d.rate                                                   AS basic_price,
            (IFNULL(SUM(rn_d.qty), 0) * po_d.rate)                    AS basic_amount,
            po_d.gst_perc                                               AS gst_rate,
            (po_d.gst_perc / 100 * IFNULL(SUM(rn_d.qty), 0) * po_d.rate) AS gst_amt,
            IFNULL(SUM(rn_d.freight_amt), 0)                           AS freight_amt,
            IFNULL(SUM(rn_d.gst_on_freight_amt), 0)                   AS gst_on_freight_amt,
            (IFNULL(SUM(rn_d.qty), 0) * po_d.rate
              + po_d.gst_perc / 100 * IFNULL(SUM(rn_d.qty), 0) * po_d.rate
              + IFNULL(SUM(rn_d.freight_amt), 0)
              + IFNULL(SUM(rn_d.gst_on_freight_amt), 0))               AS total_amount
        FROM purchase_voucher_header po
        LEFT JOIN voucher_type_master vt ON po.voucher_type_id = vt.voucher_type_id
        LEFT JOIN supplier_master s      ON po.supp_id = s.supp_id
        JOIN  purchase_voucher_detail po_d ON po_d.pv_id = po.pv_id
        JOIN  item_master i                ON po_d.item_id = i.item_id
        LEFT JOIN unit_master u            ON po_d.unit_id = u.unit_id
        LEFT JOIN location_master l        ON po_d.location_id = l.location_id
        LEFT JOIN purchase_voucher_header rn
            ON rn.linked_po_id = CAST(po.pv_id AS CHAR)
        LEFT JOIN purchase_voucher_detail rn_d
            ON rn_d.pv_id = rn.pv_id
           AND rn_d.item_id = po_d.item_id
        WHERE po.voucher_category = 'Purchase Order'
          AND (:branchFilter IS NULL OR po.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR po.supp_id = :suppId)
          AND (:fromDate IS NULL OR po.pv_date >= :fromDate)
          AND (:toDate IS NULL OR po.pv_date <= :toDate)
        GROUP BY po.pv_id, po.pv_date, s.supp_name, l.location_name,
                 po.voucher_number, po_d.item_id, i.item_desc, u.unit_desc,
                 po_d.ordered_qty, po_d.rate, po_d.gross_amount, po_d.net_amount, po_d.gst_perc
        ORDER BY po.pv_date DESC, po.pv_id, po_d.item_id
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findPvVsGrn(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("suppId") String suppId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // ========================================================================
    // REPORT 22: STOCK SUMMARY REPORT (TallyPrime-style period-end inventory)
    // ========================================================================

    /**
     * Aggregates period stock movements per item broken down by transaction sub-type.
     * LEFT JOINs dept_transfer_header twice to distinguish third-party vs inhouse transfers.
     *
     * Column index map:
     * [0] item_id   [1] item_desc   [2] group_id   [3] group_desc
     * [4] unit_id   [5] gst_perc
     * [6] purchase_qty      [7] purchase_value
     * [8] sales_return_qty  [9] sales_return_value
     * [10] ti_3p_qty        [11] ti_3p_value
     * [12] ti_inhouse_qty   [13] ti_inhouse_value
     * [14] sales_qty        [15] sales_value
     * [16] pur_return_qty   [17] pur_return_value
     * [18] to_3p_qty        [19] to_3p_value
     * [20] to_inhouse_qty   [21] to_inhouse_value
     */
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            i.group_id,
            g.group_desc,
            i.unit_id,
            i.gst_perc,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('GRN',
                              'PV_RECEIPT_NOTE', 'PV_MATERIAL_IN',
                              'PV_PHYSICAL_STOCK', 'PV_STOCK_JOURNAL')
                              THEN msl.qty_in ELSE 0 END), 0)
                AS purchase_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('GRN',
                              'PV_RECEIPT_NOTE', 'PV_MATERIAL_IN',
                              'PV_PHYSICAL_STOCK', 'PV_STOCK_JOURNAL')
                              THEN msl.qty_in * msl.rate ELSE 0 END), 0)
                AS purchase_value,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('ISSUE_REVERSAL',
                              'PV_DELIVERY_NOTE', 'PV_MATERIAL_OUT', 'PV_REJECTION_IN')
                              THEN msl.qty_in ELSE 0 END), 0)
                AS sales_return_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('ISSUE_REVERSAL',
                              'PV_DELIVERY_NOTE', 'PV_MATERIAL_OUT', 'PV_REJECTION_IN')
                              THEN msl.qty_in * msl.rate ELSE 0 END), 0)
                AS sales_return_value,
            COALESCE(SUM(CASE WHEN (msl.txn_type = 'DEPT_TRANSFER_IN'
                              AND dth_in.third_party_supplier_id IS NOT NULL)
                              OR msl.txn_type IN ('PV_JW_IN', 'PV_JW_OUT_REVERSAL')
                              THEN msl.qty_in ELSE 0 END), 0)
                AS ti_3p_qty,
            COALESCE(SUM(CASE WHEN (msl.txn_type = 'DEPT_TRANSFER_IN'
                              AND dth_in.third_party_supplier_id IS NOT NULL)
                              OR msl.txn_type IN ('PV_JW_IN', 'PV_JW_OUT_REVERSAL')
                              THEN msl.qty_in * msl.rate ELSE 0 END), 0)
                AS ti_3p_value,
            COALESCE(SUM(CASE WHEN msl.txn_type = 'TRANSFER_IN'
                              OR (msl.txn_type = 'DEPT_TRANSFER_IN' AND dth_in.third_party_supplier_id IS NULL)
                              OR msl.txn_type = 'PV_DETAILS_TRANSFER_IN'
                              THEN msl.qty_in ELSE 0 END), 0)
                AS ti_inhouse_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type = 'TRANSFER_IN'
                              OR (msl.txn_type = 'DEPT_TRANSFER_IN' AND dth_in.third_party_supplier_id IS NULL)
                              OR msl.txn_type = 'PV_DETAILS_TRANSFER_IN'
                              THEN msl.qty_in * msl.rate ELSE 0 END), 0)
                AS ti_inhouse_value,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('ISSUE',
                              'PV_DELIVERY_NOTE', 'PV_MATERIAL_OUT')
                              THEN msl.qty_out ELSE 0 END), 0)
                AS sales_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('ISSUE',
                              'PV_DELIVERY_NOTE', 'PV_MATERIAL_OUT')
                              THEN msl.qty_out * msl.rate ELSE 0 END), 0)
                AS sales_value,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('GRN_REVERSAL',
                              'PV_RECEIPT_NOTE', 'PV_MATERIAL_IN',
                              'PV_REJECTION_OUT',
                              'PV_PHYSICAL_STOCK', 'PV_STOCK_JOURNAL')
                              THEN msl.qty_out ELSE 0 END), 0)
                AS pur_return_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type IN ('GRN_REVERSAL',
                              'PV_RECEIPT_NOTE', 'PV_MATERIAL_IN',
                              'PV_REJECTION_OUT',
                              'PV_PHYSICAL_STOCK', 'PV_STOCK_JOURNAL')
                              THEN msl.qty_out * msl.rate ELSE 0 END), 0)
                AS pur_return_value,
            COALESCE(SUM(CASE WHEN (msl.txn_type = 'DEPT_TRANSFER_OUT'
                              AND dth_out.third_party_supplier_id IS NOT NULL)
                              OR msl.txn_type IN ('PV_JW_OUT', 'PV_JW_IN_REVERSAL')
                              THEN msl.qty_out ELSE 0 END), 0)
                AS to_3p_qty,
            COALESCE(SUM(CASE WHEN (msl.txn_type = 'DEPT_TRANSFER_OUT'
                              AND dth_out.third_party_supplier_id IS NOT NULL)
                              OR msl.txn_type IN ('PV_JW_OUT', 'PV_JW_IN_REVERSAL')
                              THEN msl.qty_out * msl.rate ELSE 0 END), 0)
                AS to_3p_value,
            COALESCE(SUM(CASE WHEN msl.txn_type = 'TRANSFER_OUT'
                              OR (msl.txn_type = 'DEPT_TRANSFER_OUT' AND dth_out.third_party_supplier_id IS NULL)
                              OR msl.txn_type = 'PV_DETAILS_TRANSFER_OUT'
                              THEN msl.qty_out ELSE 0 END), 0)
                AS to_inhouse_qty,
            COALESCE(SUM(CASE WHEN msl.txn_type = 'TRANSFER_OUT'
                              OR (msl.txn_type = 'DEPT_TRANSFER_OUT' AND dth_out.third_party_supplier_id IS NULL)
                              OR msl.txn_type = 'PV_DETAILS_TRANSFER_OUT'
                              THEN msl.qty_out * msl.rate ELSE 0 END), 0)
                AS to_inhouse_value
        FROM material_stock_ledger msl
        JOIN item_master i ON msl.item_id = i.item_id
        JOIN group_master g ON i.group_id = g.group_id
        LEFT JOIN dept_transfer_header dth_in
            ON msl.txn_type = 'DEPT_TRANSFER_IN' AND msl.ref_id = dth_in.dept_transfer_id
        LEFT JOIN dept_transfer_header dth_out
            ON msl.txn_type = 'DEPT_TRANSFER_OUT' AND msl.ref_id = dth_out.dept_transfer_id
        WHERE (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND msl.txn_date >= :fromDate
          AND msl.txn_date <= :toDate
          AND (:groupId IS NULL OR i.group_id = :groupId)
          AND (:itemId IS NULL OR msl.item_id = :itemId)
        GROUP BY msl.item_id, i.item_desc, i.group_id, g.group_desc, i.unit_id, i.gst_perc
        ORDER BY g.group_desc, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findStockSummaryMovements(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("groupId") String groupId,
            @Param("itemId") String itemId
    );

    /**
     * Dynamic stock summary movements: one row per (item × reportSummaryTitle).
     * Joins ledger → purchase_voucher_header → voucher_type_master to resolve column titles.
     * Only includes ledger entries whose PV has a voucher type with a non-null reportSummaryTitle.
     *
     * Column index map:
     * [0] item_id   [1] item_desc   [2] group_id   [3] group_desc
     * [4] unit_id   [5] gst_perc
     * [6] report_summary_title   [7] voucher_category
     * [8] total_qty_in   [9] total_value_in   [10] total_qty_out   [11] total_value_out
     */
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            i.group_id,
            g.group_desc,
            i.unit_id,
            i.gst_perc,
            vtm.report_summary_title,
            vtm.voucher_category,
            COALESCE(SUM(msl.qty_in),  0)             AS total_qty_in,
            COALESCE(SUM(msl.qty_in  * msl.rate), 0)  AS total_value_in,
            COALESCE(SUM(msl.qty_out), 0)             AS total_qty_out,
            COALESCE(SUM(msl.qty_out * msl.rate), 0)  AS total_value_out
        FROM material_stock_ledger msl
        JOIN item_master i ON msl.item_id = i.item_id
        JOIN group_master g ON i.group_id = g.group_id
        JOIN purchase_voucher_header pvh ON msl.ref_id = pvh.pv_id
        JOIN voucher_type_master vtm
            ON pvh.voucher_type_id = vtm.voucher_type_id
           AND vtm.report_summary_title IS NOT NULL
        WHERE (:branchFilter IS NULL OR msl.branch_id IN (:branchIds))
          AND msl.txn_date >= :fromDate
          AND msl.txn_date <= :toDate
          AND (:groupId IS NULL OR i.group_id = :groupId)
          AND (:itemId IS NULL OR msl.item_id = :itemId)
        GROUP BY
            msl.item_id, i.item_desc, i.group_id, g.group_desc, i.unit_id, i.gst_perc,
            vtm.report_summary_title, vtm.voucher_category
        ORDER BY g.group_desc, i.item_desc, vtm.report_summary_title
        """, nativeQuery = true)
    List<Object[]> findStockSummaryMovementsDynamic(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("groupId") String groupId,
            @Param("itemId") String itemId
    );

    /**
     * Opening balance per item: the balance_qty from the last ledger entry before fromDate,
     * summed across branches/locations.
     *
     * Column index map:
     * [0] item_id   [1] item_desc   [2] opening_qty   [3] opening_rate
     * [4] group_id  [5] group_desc  [6] unit_id        [7] gst_perc
     */
    @Query(value = """
        SELECT
            ob.item_id,
            i.item_desc,
            SUM(ob.balance_qty)  AS opening_qty,
            CASE WHEN SUM(ob.balance_qty) = 0 THEN 0
                 ELSE SUM(ob.balance_qty * ob.rate) / SUM(ob.balance_qty)
            END AS opening_rate,
            i.group_id,
            g.group_desc,
            i.unit_id,
            i.gst_perc
        FROM (
            SELECT msl.branch_id, msl.item_id, msl.location_id, msl.balance_qty, msl.rate
            FROM material_stock_ledger msl
            INNER JOIN (
                SELECT branch_id, item_id, location_id, MAX(ledger_id) AS max_ledger_id
                FROM material_stock_ledger
                WHERE (txn_date < :fromDate
                       OR (txn_date = :fromDate AND txn_type IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')))
                  AND (:branchFilter IS NULL OR branch_id IN (:branchIds))
                GROUP BY branch_id, item_id, location_id
            ) latest ON msl.ledger_id = latest.max_ledger_id
        ) ob
        JOIN item_master i ON ob.item_id = i.item_id
        JOIN group_master g ON i.group_id = g.group_id
        WHERE (:groupId IS NULL OR i.group_id = :groupId)
          AND (:itemId IS NULL OR ob.item_id = :itemId)
        GROUP BY ob.item_id, i.item_desc, i.group_id, g.group_desc, i.unit_id, i.gst_perc
        """, nativeQuery = true)
    List<Object[]> findStockSummaryOpening(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("fromDate") LocalDate fromDate,
            @Param("groupId") String groupId,
            @Param("itemId") String itemId
    );

    // ========================================================================
    // REPORT 21: PV ITEM FULFILLMENT (item-level ordered vs received vs pending)
    // ========================================================================
    @Query(value = """
        SELECT
            pvh.pv_id,
            pvh.voucher_number,
            pvh.pv_date,
            pvh.branch_id,
            b.branch_name,
            pvh.supp_id,
            s.supp_name,
            pvh.status,
            pvd.item_id,
            i.item_desc,
            pvd.qty,
            pvd.rate,
            pvd.net_amount,
            COALESCE(SUM(gd.qty_received), 0) AS total_received_qty,
            (pvd.qty - COALESCE(SUM(gd.qty_received), 0)) AS pending_qty,
            CASE WHEN pvd.qty = 0 THEN 100.00
                 ELSE ROUND(COALESCE(SUM(gd.qty_received), 0) / pvd.qty * 100, 2)
            END AS fulfillment_percent
        FROM purchase_voucher_detail pvd
        JOIN purchase_voucher_header pvh ON pvd.pv_id = pvh.pv_id
        JOIN branch_master b ON pvh.branch_id = b.branch_id
        JOIN supplier_master s ON pvh.supp_id = s.supp_id
        JOIN item_master i ON pvd.item_id = i.item_id
        LEFT JOIN grn_header gh ON gh.pv_id = pvh.pv_id AND gh.status = 'POSTED'
        LEFT JOIN grn_detail gd ON gd.grn_id = gh.grn_id AND gd.item_id = pvd.item_id
        WHERE (:pvId IS NULL OR pvh.pv_id = :pvId)
          AND (:branchFilter IS NULL OR pvh.branch_id IN (:branchIds))
          AND (:suppId IS NULL OR pvh.supp_id = :suppId)
          AND (:fromDate IS NULL OR pvh.pv_date >= :fromDate)
          AND (:toDate IS NULL OR pvh.pv_date <= :toDate)
          AND (:status IS NULL OR pvh.status = :status)
        GROUP BY pvh.pv_id, pvh.voucher_number, pvh.pv_date, pvh.branch_id, b.branch_name,
                 pvh.supp_id, s.supp_name, pvh.status,
                 pvd.item_id, i.item_desc, pvd.qty, pvd.rate, pvd.net_amount
        ORDER BY pvh.pv_date DESC, pvh.pv_id, pvd.item_id
        """, nativeQuery = true)
    List<Object[]> findPvItemFulfillment(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("pvId") Long pvId,
            @Param("suppId") String suppId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") String status
    );

    // ========================================================================
    // ITEM STOCK POSITION (TallyPrime-style Stock Item Vouchers)
    // ========================================================================

    /**
     * Opening balance for an item/branch before the given fromDate.
     * Columns: [0]=opening_qty, [1]=opening_rate
     */
    @Query(value = """
        SELECT
            COALESCE(SUM(l.qty_in) - SUM(l.qty_out), 0) AS opening_qty,
            CASE WHEN SUM(l.qty_in) > 0
                 THEN SUM(l.qty_in * l.rate) / SUM(l.qty_in)
                 ELSE 0 END AS opening_rate
        FROM material_stock_ledger l
        WHERE l.branch_id = :branchId
          AND l.item_id   = :itemId
          AND (:locationId IS NULL OR l.location_id = :locationId)
          AND l.txn_date  < :fromDate
        """, nativeQuery = true)
    List<Object[]> findOpeningBalanceForItemPosition(
            @Param("branchId") String branchId,
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate);

    /**
     * Chronological transactions for an item/branch within date range, enriched with
     * party name, voucher type name, and voucher number via LEFT JOINs.
     * Columns: [0]=ledger_id, [1]=txn_date, [2]=txn_type, [3]=qty_in, [4]=qty_out,
     *          [5]=rate, [6]=particulars, [7]=vch_type_name, [8]=vch_no
     */
    @Query(value = """
        SELECT
            l.ledger_id, l.txn_date, l.txn_type, l.qty_in, l.qty_out, l.rate,
            CASE
                WHEN l.txn_type = 'GRN'
                     THEN sm_grn.supp_name
                WHEN l.txn_type = 'ISSUE'
                     THEN COALESCE(sm_iss.supp_name, ih.issued_to)
                WHEN l.txn_type = 'TRANSFER_IN'
                     THEN fb_st.branch_name
                WHEN l.txn_type = 'TRANSFER_OUT'
                     THEN tb_st.branch_name
                WHEN l.txn_type IN ('DEPT_TRANSFER_IN', 'DEPT_TRANSFER_OUT')
                     THEN COALESCE(sm_dt.supp_name,
                                   CASE WHEN l.txn_type = 'DEPT_TRANSFER_IN'
                                        THEN 'Dept Transfer In'
                                        ELSE 'Dept Transfer Out' END)
                WHEN l.txn_type LIKE 'PV_%'
                     THEN sm_pv.supp_name
                ELSE l.txn_type
            END AS particulars,
            CASE
                WHEN l.txn_type = 'GRN'
                     THEN COALESCE(vtm_grn.voucher_type_name, 'GRN')
                WHEN l.txn_type = 'ISSUE'
                     THEN COALESCE(vtm_iss.voucher_type_name, 'Issue')
                WHEN l.txn_type IN ('TRANSFER_IN', 'TRANSFER_OUT')
                     THEN COALESCE(vtm_st.voucher_type_name, 'Stock Transfer')
                WHEN l.txn_type IN ('DEPT_TRANSFER_IN', 'DEPT_TRANSFER_OUT')
                     THEN COALESCE(vtm_dt.voucher_type_name, 'Dept Transfer')
                WHEN l.txn_type LIKE 'PV_%'
                     THEN COALESCE(vtm_pv.voucher_type_name, l.txn_type)
                ELSE l.txn_type
            END AS vch_type_name,
            CASE
                WHEN l.txn_type = 'GRN'                                      THEN gh.voucher_number
                WHEN l.txn_type = 'ISSUE'                                    THEN ih.voucher_number
                WHEN l.txn_type IN ('TRANSFER_IN', 'TRANSFER_OUT')           THEN sth.voucher_number
                WHEN l.txn_type IN ('DEPT_TRANSFER_IN', 'DEPT_TRANSFER_OUT') THEN dth.voucher_number
                WHEN l.txn_type LIKE 'PV_%'                                  THEN pvh.voucher_number
                ELSE NULL
            END AS vch_no
        FROM material_stock_ledger l
        LEFT JOIN grn_header gh
               ON l.txn_type = 'GRN' AND l.ref_id = gh.grn_id
        LEFT JOIN supplier_master sm_grn ON gh.supp_id = sm_grn.supp_id
        LEFT JOIN voucher_type_master vtm_grn ON gh.voucher_type_id = vtm_grn.voucher_type_id
        LEFT JOIN issue_header ih
               ON l.txn_type = 'ISSUE' AND l.ref_id = ih.issue_id
        LEFT JOIN supplier_master sm_iss ON ih.supp_id = sm_iss.supp_id
        LEFT JOIN voucher_type_master vtm_iss ON ih.voucher_type_id = vtm_iss.voucher_type_id
        LEFT JOIN stock_transfer_header sth
               ON l.txn_type IN ('TRANSFER_IN', 'TRANSFER_OUT') AND l.ref_id = sth.transfer_id
        LEFT JOIN branch_master fb_st ON sth.from_branch = fb_st.branch_id
        LEFT JOIN branch_master tb_st ON sth.to_branch   = tb_st.branch_id
        LEFT JOIN voucher_type_master vtm_st ON sth.voucher_type_id = vtm_st.voucher_type_id
        LEFT JOIN dept_transfer_header dth
               ON l.txn_type IN ('DEPT_TRANSFER_IN', 'DEPT_TRANSFER_OUT') AND l.ref_id = dth.dept_transfer_id
        LEFT JOIN supplier_master sm_dt ON dth.third_party_supplier_id = sm_dt.supp_id
        LEFT JOIN voucher_type_master vtm_dt ON dth.voucher_type_id = vtm_dt.voucher_type_id
        LEFT JOIN purchase_voucher_header pvh
               ON l.txn_type LIKE 'PV_%' AND l.ref_id = pvh.pv_id
        LEFT JOIN supplier_master sm_pv ON pvh.supp_id = sm_pv.supp_id
        LEFT JOIN voucher_type_master vtm_pv ON pvh.voucher_type_id = vtm_pv.voucher_type_id
        WHERE l.branch_id = :branchId
          AND l.item_id   = :itemId
          AND (:locationId IS NULL OR l.location_id = :locationId)
          AND l.txn_date BETWEEN :fromDate AND :toDate
        ORDER BY l.txn_date ASC, l.ledger_id ASC
        """, nativeQuery = true)
    List<Object[]> findItemStockPositionTransactions(
            @Param("branchId") String branchId,
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // REPORT: ITEM-WISE STOCK POSITION (BRANCH + GODOWN UNION)
    // ========================================================================
    @Query(value = """
        SELECT
            'BRANCH'                           AS location_type,
            bms.item_id,
            i.item_desc,
            bms.branch_id,
            b.branch_name,
            bms.location_id,
            l.location_name,
            NULL                               AS supp_id,
            NULL                               AS supp_name,
            NULL                               AS godown_id,
            NULL                               AS godown_name,
            bms.qty_on_hand                    AS qty,
            bms.avg_cost,
            (bms.qty_on_hand * bms.avg_cost)   AS stock_value
        FROM branch_material_stock bms
        JOIN branch_master b        ON bms.branch_id   = b.branch_id
        JOIN item_master i          ON bms.item_id     = i.item_id
        LEFT JOIN location_master l ON bms.location_id = l.location_id
        WHERE bms.qty_on_hand > 0
          AND (:itemId IS NULL OR bms.item_id = :itemId)
          AND (:branchFilter IS NULL OR bms.branch_id IN (:branchIds))

        UNION ALL

        SELECT
            'GODOWN'                           AS location_type,
            gis.item_id,
            i.item_desc,
            NULL                               AS branch_id,
            NULL                               AS branch_name,
            NULL                               AS location_id,
            NULL                               AS location_name,
            sgm.supp_id,
            sm.supp_name,
            CAST(gis.godown_id AS CHAR)        AS godown_id,
            sgm.godown_name,
            gis.qty                            AS qty,
            NULL                               AS avg_cost,
            NULL                               AS stock_value
        FROM godown_item_stock gis
        JOIN supplier_godown_map sgm ON gis.godown_id = sgm.id
        JOIN supplier_master sm      ON sgm.supp_id   = sm.supp_id
        JOIN item_master i           ON gis.item_id   = i.item_id
        WHERE gis.qty > 0
          AND (:itemId IS NULL OR gis.item_id = :itemId)
          AND (:suppId IS NULL OR sgm.supp_id = :suppId)

        ORDER BY item_desc, location_type, branch_name, supp_name
        """, nativeQuery = true)
    List<Object[]> findItemStockPosition(
            @Param("branchIds") List<String> branchIds,
            @Param("branchFilter") String branchFilter,
            @Param("itemId") String itemId,
            @Param("suppId") String suppId
    );

    // ========================================================================
    // ITEM MOVEMENT ANALYSIS (TallyPrime-style grouped by category + party)
    // ========================================================================
    /**
     * Aggregated movement analysis for a specific item/branch/date range.
     * Groups by (txn_type, particulars). Party name derived via LEFT JOINs.
     * GRN types return separate basic_rate (from grn_detail.rate) and
     * effective_rate (from grn_detail.net_amount); all others use ledger rate.
     *
     * Columns: [0]=txn_type, [1]=particulars, [2]=total_qty,
     *          [3]=basic_rate, [4]=effective_rate, [5]=total_value
     */
    @Query(value = """
        SELECT
            l.txn_type,
            CASE
                WHEN l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
                     THEN COALESCE(sm_grn.supp_name, l.txn_type)
                WHEN l.txn_type IN ('ISSUE','ISSUE_REVERSAL','ISSUE_CORRECTION')
                     THEN COALESCE(sm_iss.supp_name, ih.issued_to, l.txn_type)
                WHEN l.txn_type IN ('TRANSFER_IN','TRANSFER_IN_REVERSAL','TRANSFER_IN_CORRECTION')
                     THEN COALESCE(fb_st.branch_name, l.txn_type)
                WHEN l.txn_type IN ('TRANSFER_OUT','TRANSFER_OUT_REVERSAL','TRANSFER_OUT_CORRECTION')
                     THEN COALESCE(tb_st.branch_name, l.txn_type)
                WHEN l.txn_type IN ('DEPT_TRANSFER_IN','DEPT_TRANSFER_IN_CORRECTION')
                     THEN COALESCE(sm_dt.supp_name, 'Dept Transfer In')
                WHEN l.txn_type IN ('DEPT_TRANSFER_OUT','DEPT_TRANSFER_OUT_CORRECTION')
                     THEN COALESCE(sm_dt.supp_name, 'Dept Transfer Out')
                WHEN l.txn_type IN ('OPENING_BALANCE','OB_REVERSAL','OB_CORRECTION')
                     THEN 'Opening Balance'
                WHEN l.txn_type LIKE 'PV_%'
                     THEN COALESCE(sm_pv.supp_name, l.txn_type)
                ELSE l.txn_type
            END AS particulars,
            SUM(CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END) AS total_qty,
            CASE
                WHEN l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
                     THEN CASE WHEN SUM(l.qty_in) > 0
                               THEN SUM(COALESCE(gd.rate, l.rate) * l.qty_in) / SUM(l.qty_in)
                               ELSE 0 END
                ELSE CASE WHEN SUM(CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END) > 0
                          THEN SUM(l.rate * CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END)
                               / SUM(CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END)
                          ELSE 0 END
            END AS basic_rate,
            CASE
                WHEN l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
                     THEN CASE WHEN SUM(l.qty_in) > 0
                               THEN SUM(COALESCE(gd.net_amount, l.rate * l.qty_in)) / SUM(l.qty_in)
                               ELSE 0 END
                ELSE CASE WHEN SUM(CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END) > 0
                          THEN SUM(l.rate * CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END)
                               / SUM(CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END)
                          ELSE 0 END
            END AS effective_rate,
            SUM(l.rate * CASE WHEN l.qty_in > 0 THEN l.qty_in ELSE l.qty_out END) AS total_value
        FROM material_stock_ledger l
        LEFT JOIN grn_header gh
               ON l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
              AND l.ref_id = gh.grn_id
        LEFT JOIN grn_detail gd
               ON l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
              AND l.ref_id = gd.grn_id
              AND gd.item_id = l.item_id
        LEFT JOIN supplier_master sm_grn ON gh.supp_id = sm_grn.supp_id
        LEFT JOIN issue_header ih
               ON l.txn_type IN ('ISSUE','ISSUE_REVERSAL','ISSUE_CORRECTION')
              AND l.ref_id = ih.issue_id
        LEFT JOIN supplier_master sm_iss ON ih.supp_id = sm_iss.supp_id
        LEFT JOIN stock_transfer_header sth
               ON l.txn_type IN ('TRANSFER_IN','TRANSFER_IN_REVERSAL','TRANSFER_IN_CORRECTION',
                                 'TRANSFER_OUT','TRANSFER_OUT_REVERSAL','TRANSFER_OUT_CORRECTION')
              AND l.ref_id = sth.transfer_id
        LEFT JOIN branch_master fb_st ON sth.from_branch = fb_st.branch_id
        LEFT JOIN branch_master tb_st ON sth.to_branch   = tb_st.branch_id
        LEFT JOIN dept_transfer_header dth
               ON l.txn_type IN ('DEPT_TRANSFER_IN','DEPT_TRANSFER_IN_CORRECTION',
                                 'DEPT_TRANSFER_OUT','DEPT_TRANSFER_OUT_CORRECTION')
              AND l.ref_id = dth.dept_transfer_id
        LEFT JOIN supplier_master sm_dt ON dth.third_party_supplier_id = sm_dt.supp_id
        LEFT JOIN purchase_voucher_header pvh
               ON l.txn_type LIKE 'PV_%'
              AND l.ref_id = pvh.pv_id
        LEFT JOIN supplier_master sm_pv ON pvh.supp_id = sm_pv.supp_id
        WHERE l.branch_id = :branchId
          AND l.item_id   = :itemId
          AND l.txn_date BETWEEN :fromDate AND :toDate
        GROUP BY l.txn_type, particulars
        ORDER BY l.txn_type, particulars
        """, nativeQuery = true)
    List<Object[]> findItemMovementAnalysis(
            @Param("branchId") String branchId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // GODOWN SUMMARY: Items with movement in date range
    // Columns: [0]=item_id, [1]=item_desc, [2]=location_id, [3]=location_name,
    //          [4]=group_id, [5]=group_desc,
    //          [6]=opening_qty, [7]=opening_rate,
    //          [8]=inward_qty, [9]=inward_value,
    //          [10]=outward_qty, [11]=outward_value
    // ========================================================================
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            msl.location_id,
            l.location_name,
            i.group_id,
            g.group_desc,
            COALESCE((
                SELECT sub.balance_qty FROM material_stock_ledger sub
                WHERE sub.branch_id   = msl.branch_id
                  AND sub.item_id     = msl.item_id
                  AND sub.location_id = msl.location_id
                  AND (sub.txn_date < :fromDate
                       OR (sub.txn_date = :fromDate AND sub.txn_type IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')))
                ORDER BY sub.ledger_id DESC LIMIT 1
            ), 0) AS opening_qty,
            COALESCE((
                SELECT sub.rate FROM material_stock_ledger sub
                WHERE sub.branch_id   = msl.branch_id
                  AND sub.item_id     = msl.item_id
                  AND sub.location_id = msl.location_id
                  AND (sub.txn_date < :fromDate
                       OR (sub.txn_date = :fromDate AND sub.txn_type IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')))
                ORDER BY sub.ledger_id DESC LIMIT 1
            ), 0) AS opening_rate,
            SUM(msl.qty_in)             AS inward_qty,
            SUM(msl.qty_in  * msl.rate) AS inward_value,
            SUM(msl.qty_out)            AS outward_qty,
            SUM(msl.qty_out * msl.rate) AS outward_value
        FROM material_stock_ledger msl
        JOIN item_master i           ON msl.item_id  = i.item_id
        LEFT JOIN group_master g     ON i.group_id   = g.group_id
        LEFT JOIN location_master l  ON msl.location_id = l.location_id
        WHERE msl.branch_id = :branchId
          AND (:locationId IS NULL OR msl.location_id = :locationId)
          AND msl.txn_date BETWEEN :fromDate AND :toDate
          AND msl.txn_type NOT IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')
        GROUP BY msl.item_id, i.item_desc, msl.location_id, l.location_name,
                 i.group_id, g.group_desc
        ORDER BY g.group_desc, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findGodownSummary(
            @Param("branchId") String branchId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // GODOWN SUMMARY: Items with opening balance but NO movement in date range
    // Columns: [0]=item_id, [1]=item_desc, [2]=location_id, [3]=location_name,
    //          [4]=group_id, [5]=group_desc,
    //          [6]=opening_qty, [7]=opening_rate
    // ========================================================================
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            msl.location_id,
            l.location_name,
            i.group_id,
            g.group_desc,
            msl.balance_qty AS opening_qty,
            msl.rate        AS opening_rate
        FROM material_stock_ledger msl
        JOIN item_master i           ON msl.item_id    = i.item_id
        LEFT JOIN group_master g     ON i.group_id     = g.group_id
        LEFT JOIN location_master l  ON msl.location_id = l.location_id
        WHERE msl.branch_id  = :branchId
          AND (:locationId IS NULL OR msl.location_id = :locationId)
          AND msl.ledger_id = (
              SELECT MAX(sub.ledger_id) FROM material_stock_ledger sub
              WHERE sub.branch_id   = msl.branch_id
                AND sub.item_id     = msl.item_id
                AND sub.location_id = msl.location_id
                AND (sub.txn_date < :fromDate
                     OR (sub.txn_date = :fromDate AND sub.txn_type IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')))
          )
          AND msl.balance_qty > 0
          AND NOT EXISTS (
              SELECT 1 FROM material_stock_ledger inrange
              WHERE inrange.branch_id   = msl.branch_id
                AND inrange.item_id     = msl.item_id
                AND inrange.location_id = msl.location_id
                AND inrange.txn_date   >= :fromDate
                AND inrange.txn_date   <= :toDate
                AND inrange.txn_type NOT IN ('OPENING_BALANCE', 'OB_REVERSAL', 'OB_CORRECTION')
          )
        ORDER BY g.group_desc, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findGodownSummaryOpeningOnly(
            @Param("branchId") String branchId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // GODOWN VOUCHER: Opening balance for a specific item + location
    // Columns: [0]=balance_qty, [1]=rate, [2]=item_desc, [3]=location_name
    // ========================================================================
    @Query(value = """
        SELECT msl.balance_qty, msl.rate, i.item_desc, l.location_name
        FROM material_stock_ledger msl
        JOIN item_master i ON msl.item_id = i.item_id
        LEFT JOIN location_master l ON msl.location_id = l.location_id
        WHERE msl.item_id     = :itemId
          AND msl.location_id = :locationId
          AND msl.txn_date    < :fromDate
        ORDER BY msl.ledger_id DESC
        LIMIT 1
        """, nativeQuery = true)
    List<Object[]> findGodownVoucherOpening(
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate);

    // ========================================================================
    // GODOWN VOUCHER: Transaction entries for a specific item + location + date range
    // Columns: [0]=ledger_id, [1]=txn_date, [2]=txn_type, [3]=vch_no,
    //          [4]=particulars, [5]=qty_in, [6]=qty_out, [7]=rate, [8]=balance_qty,
    //          [9]=item_desc, [10]=location_name
    // ========================================================================
    @Query(value = """
        SELECT
            l.ledger_id,
            l.txn_date,
            l.txn_type,
            COALESCE(gh.voucher_number, ih.voucher_number, pvh.voucher_number) AS vch_no,
            CASE
                WHEN l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
                     THEN COALESCE(sm_grn.supp_name, l.txn_type)
                WHEN l.txn_type IN ('ISSUE','ISSUE_REVERSAL','ISSUE_CORRECTION')
                     THEN COALESCE(sm_iss.supp_name, ih.issued_to, l.txn_type)
                WHEN l.txn_type IN ('TRANSFER_IN','TRANSFER_IN_REVERSAL','TRANSFER_IN_CORRECTION')
                     THEN COALESCE(fb_st.branch_name, l.txn_type)
                WHEN l.txn_type IN ('TRANSFER_OUT','TRANSFER_OUT_REVERSAL','TRANSFER_OUT_CORRECTION')
                     THEN COALESCE(tb_st.branch_name, l.txn_type)
                WHEN l.txn_type IN ('DEPT_TRANSFER_IN','DEPT_TRANSFER_IN_CORRECTION')
                     THEN COALESCE(sm_dt.supp_name, 'Dept Transfer In')
                WHEN l.txn_type IN ('DEPT_TRANSFER_OUT','DEPT_TRANSFER_OUT_CORRECTION')
                     THEN COALESCE(sm_dt.supp_name, 'Dept Transfer Out')
                WHEN l.txn_type IN ('OPENING_BALANCE','OB_REVERSAL','OB_CORRECTION')
                     THEN 'Opening Balance'
                WHEN l.txn_type LIKE 'PV_%'
                     THEN COALESCE(sm_pv.supp_name, l.txn_type)
                ELSE l.txn_type
            END AS particulars,
            l.qty_in,
            l.qty_out,
            l.rate,
            l.balance_qty,
            i.item_desc,
            loc.location_name
        FROM material_stock_ledger l
        JOIN item_master i ON l.item_id = i.item_id
        LEFT JOIN location_master loc ON l.location_id = loc.location_id
        LEFT JOIN grn_header gh
               ON l.txn_type IN ('GRN','GRN_REVERSAL','GRN_CORRECTION')
              AND l.ref_id = gh.grn_id
        LEFT JOIN supplier_master sm_grn ON gh.supp_id = sm_grn.supp_id
        LEFT JOIN issue_header ih
               ON l.txn_type IN ('ISSUE','ISSUE_REVERSAL','ISSUE_CORRECTION')
              AND l.ref_id = ih.issue_id
        LEFT JOIN supplier_master sm_iss ON ih.supp_id = sm_iss.supp_id
        LEFT JOIN stock_transfer_header sth
               ON l.txn_type IN ('TRANSFER_IN','TRANSFER_IN_REVERSAL','TRANSFER_IN_CORRECTION',
                                 'TRANSFER_OUT','TRANSFER_OUT_REVERSAL','TRANSFER_OUT_CORRECTION')
              AND l.ref_id = sth.transfer_id
        LEFT JOIN branch_master fb_st ON sth.from_branch = fb_st.branch_id
        LEFT JOIN branch_master tb_st ON sth.to_branch   = tb_st.branch_id
        LEFT JOIN dept_transfer_header dth
               ON l.txn_type IN ('DEPT_TRANSFER_IN','DEPT_TRANSFER_IN_CORRECTION',
                                 'DEPT_TRANSFER_OUT','DEPT_TRANSFER_OUT_CORRECTION')
              AND l.ref_id = dth.dept_transfer_id
        LEFT JOIN supplier_master sm_dt ON dth.third_party_supplier_id = sm_dt.supp_id
        LEFT JOIN purchase_voucher_header pvh
               ON l.txn_type LIKE 'PV_%'
              AND l.ref_id = pvh.pv_id
        LEFT JOIN supplier_master sm_pv ON pvh.supp_id = sm_pv.supp_id
        WHERE l.item_id     = :itemId
          AND l.location_id = :locationId
          AND (:fromDate IS NULL OR l.txn_date >= :fromDate)
          AND (:toDate   IS NULL OR l.txn_date <= :toDate)
        ORDER BY l.txn_date ASC, l.ledger_id ASC
        """, nativeQuery = true)
    List<Object[]> findGodownVoucherEntries(
            @Param("itemId") String itemId,
            @Param("locationId") String locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // SUPPLIER GODOWN SUMMARY — items with movement in period
    // Inward to godown  = qty_out in msl (goods sent OUT of branch TO supplier godown)
    // Outward from godown = qty_in in msl (goods received back INTO branch FROM godown)
    // Covers all godown-affecting PV categories: JW, Delivery, Receipt, Material In/Out, Rejection In/Out
    // Filter: godownId (supplier_godown_map.id) AND/OR suppId. At least one must be non-null.
    // Column index: [0] item_id, [1] item_desc, [2] group_id, [3] group_desc, [4] unit_desc,
    //               [5] opening_qty, [6] inward_qty, [7] inward_value, [8] outward_qty, [9] outward_value
    // ========================================================================
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            i.group_id,
            g.group_desc,
            u.unit_desc,
            COALESCE((
                SELECT SUM(sub.qty_out) - SUM(sub.qty_in)
                FROM material_stock_ledger sub
                JOIN purchase_voucher_header pvh2
                  ON sub.ref_id = pvh2.pv_id
                 AND sub.txn_type IN (
                     'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
                     'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
                     'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
                     'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
                     'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
                     'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
                     'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
                LEFT JOIN supplier_godown_map sgm_d2 ON pvh2.destination_godown_id = sgm_d2.id
                LEFT JOIN supplier_godown_map sgm_s2 ON pvh2.source_godown_id      = sgm_s2.id
                WHERE (:godownId IS NULL OR pvh2.destination_godown_id = :godownId OR pvh2.source_godown_id = :godownId)
                  AND (:suppId IS NULL OR sgm_d2.supp_id = :suppId OR sgm_s2.supp_id = :suppId)
                  AND (pvh2.destination_godown_id IS NOT NULL OR pvh2.source_godown_id IS NOT NULL)
                  AND sub.item_id = msl.item_id
                  AND sub.txn_date < :fromDate
            ), 0)                              AS opening_qty,
            SUM(msl.qty_out)                   AS inward_qty,
            SUM(msl.qty_out * msl.rate)        AS inward_value,
            SUM(msl.qty_in)                    AS outward_qty,
            SUM(msl.qty_in  * msl.rate)        AS outward_value
        FROM material_stock_ledger msl
        JOIN purchase_voucher_header pvh
          ON msl.ref_id = pvh.pv_id
         AND msl.txn_type IN (
             'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
             'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
             'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
             'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
             'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
             'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
             'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
        LEFT JOIN supplier_godown_map sgm_dst ON pvh.destination_godown_id = sgm_dst.id
        LEFT JOIN supplier_godown_map sgm_src ON pvh.source_godown_id      = sgm_src.id
        JOIN item_master i       ON msl.item_id = i.item_id
        LEFT JOIN group_master g ON i.group_id  = g.group_id
        LEFT JOIN unit_master u  ON i.unit_id   = u.unit_id
        WHERE (:godownId IS NULL OR pvh.destination_godown_id = :godownId OR pvh.source_godown_id = :godownId)
          AND (:suppId IS NULL OR sgm_dst.supp_id = :suppId OR sgm_src.supp_id = :suppId)
          AND (pvh.destination_godown_id IS NOT NULL OR pvh.source_godown_id IS NOT NULL)
          AND (:itemId IS NULL OR msl.item_id = :itemId)
          AND msl.txn_date BETWEEN :fromDate AND :toDate
        GROUP BY msl.item_id, i.item_desc, i.group_id, g.group_desc, u.unit_desc
        ORDER BY g.group_desc, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findSupplierGodownSummary(
            @Param("suppId") String suppId,
            @Param("godownId") Long godownId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // SUPPLIER GODOWN SUMMARY — items with opening balance only (no in-period movement)
    // Column index: [0] item_id, [1] item_desc, [2] group_id, [3] group_desc,
    //               [4] opening_qty, [5] opening_rate
    // ========================================================================
    @Query(value = """
        SELECT
            msl.item_id,
            i.item_desc,
            i.group_id,
            g.group_desc,
            SUM(msl.qty_out) - SUM(msl.qty_in) AS opening_qty,
            (SELECT sub.rate FROM material_stock_ledger sub
             JOIN purchase_voucher_header pvh3
               ON sub.ref_id = pvh3.pv_id
              AND sub.txn_type IN (
                  'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
                  'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
                  'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
                  'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
                  'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
                  'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
                  'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
             LEFT JOIN supplier_godown_map sd3 ON pvh3.destination_godown_id = sd3.id
             LEFT JOIN supplier_godown_map ss3 ON pvh3.source_godown_id      = ss3.id
             WHERE (:godownId IS NULL OR pvh3.destination_godown_id = :godownId OR pvh3.source_godown_id = :godownId)
               AND (:suppId IS NULL OR sd3.supp_id = :suppId OR ss3.supp_id = :suppId)
               AND (pvh3.destination_godown_id IS NOT NULL OR pvh3.source_godown_id IS NOT NULL)
               AND sub.item_id = msl.item_id
               AND sub.txn_date < :fromDate
             ORDER BY sub.ledger_id DESC LIMIT 1) AS opening_rate
        FROM material_stock_ledger msl
        JOIN purchase_voucher_header pvh
          ON msl.ref_id = pvh.pv_id
         AND msl.txn_type IN (
             'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
             'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
             'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
             'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
             'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
             'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
             'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
        LEFT JOIN supplier_godown_map sgm_dst ON pvh.destination_godown_id = sgm_dst.id
        LEFT JOIN supplier_godown_map sgm_src ON pvh.source_godown_id      = sgm_src.id
        JOIN item_master i       ON msl.item_id = i.item_id
        LEFT JOIN group_master g ON i.group_id  = g.group_id
        WHERE (:godownId IS NULL OR pvh.destination_godown_id = :godownId OR pvh.source_godown_id = :godownId)
          AND (:suppId IS NULL OR sgm_dst.supp_id = :suppId OR sgm_src.supp_id = :suppId)
          AND (pvh.destination_godown_id IS NOT NULL OR pvh.source_godown_id IS NOT NULL)
          AND (:itemId IS NULL OR msl.item_id = :itemId)
          AND msl.txn_date < :fromDate
        GROUP BY msl.item_id, i.item_desc, i.group_id, g.group_desc
        HAVING (SUM(msl.qty_out) - SUM(msl.qty_in)) > 0
          AND msl.item_id NOT IN (
              SELECT DISTINCT sub2.item_id FROM material_stock_ledger sub2
              JOIN purchase_voucher_header pvh2
                ON sub2.ref_id = pvh2.pv_id
               AND sub2.txn_type IN (
                   'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
                   'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
                   'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
                   'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
                   'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
                   'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
                   'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
              LEFT JOIN supplier_godown_map sgm_d2 ON pvh2.destination_godown_id = sgm_d2.id
              LEFT JOIN supplier_godown_map sgm_s2 ON pvh2.source_godown_id = sgm_s2.id
              WHERE (:godownId IS NULL OR pvh2.destination_godown_id = :godownId OR pvh2.source_godown_id = :godownId)
                AND (:suppId IS NULL OR sgm_d2.supp_id = :suppId OR sgm_s2.supp_id = :suppId)
                AND (pvh2.destination_godown_id IS NOT NULL OR pvh2.source_godown_id IS NOT NULL)
                AND sub2.txn_date BETWEEN :fromDate AND :toDate
          )
        ORDER BY g.group_desc, i.item_desc
        """, nativeQuery = true)
    List<Object[]> findSupplierGodownOpeningOnly(
            @Param("suppId") String suppId,
            @Param("godownId") Long godownId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ========================================================================
    // SUPPLIER GODOWN VOUCHER — opening balance before fromDate for a specific item
    // Column index: [0] opening_qty, [1] opening_rate
    // ========================================================================
    @Query(value = """
        SELECT
            COALESCE(SUM(msl.qty_out) - SUM(msl.qty_in), 0) AS opening_qty,
            COALESCE((
                SELECT sub.rate FROM material_stock_ledger sub
                JOIN purchase_voucher_header pvh2
                  ON sub.ref_id = pvh2.pv_id
                 AND sub.txn_type IN (
                     'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
                     'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
                     'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
                     'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
                     'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
                     'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
                     'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
                LEFT JOIN supplier_godown_map sd2 ON pvh2.destination_godown_id = sd2.id
                LEFT JOIN supplier_godown_map ss2 ON pvh2.source_godown_id      = ss2.id
                WHERE (:godownId IS NULL OR pvh2.destination_godown_id = :godownId OR pvh2.source_godown_id = :godownId)
                  AND (:suppId IS NULL OR sd2.supp_id = :suppId OR ss2.supp_id = :suppId)
                  AND (pvh2.destination_godown_id IS NOT NULL OR pvh2.source_godown_id IS NOT NULL)
                  AND sub.item_id = :itemId
                  AND sub.txn_date < :fromDate
                ORDER BY sub.ledger_id DESC LIMIT 1
            ), 0) AS opening_rate
        FROM material_stock_ledger msl
        JOIN purchase_voucher_header pvh
          ON msl.ref_id = pvh.pv_id
         AND msl.txn_type IN (
             'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
             'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
             'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
             'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
             'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
             'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
             'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
        LEFT JOIN supplier_godown_map sgm_dst ON pvh.destination_godown_id = sgm_dst.id
        LEFT JOIN supplier_godown_map sgm_src ON pvh.source_godown_id      = sgm_src.id
        WHERE (:godownId IS NULL OR pvh.destination_godown_id = :godownId OR pvh.source_godown_id = :godownId)
          AND (:suppId IS NULL OR sgm_dst.supp_id = :suppId OR sgm_src.supp_id = :suppId)
          AND (pvh.destination_godown_id IS NOT NULL OR pvh.source_godown_id IS NOT NULL)
          AND msl.item_id = :itemId
          AND msl.txn_date < :fromDate
        """, nativeQuery = true)
    List<Object[]> findSupplierGodownVoucherOpening(
            @Param("suppId") String suppId,
            @Param("godownId") Long godownId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate);

    // ========================================================================
    // SUPPLIER GODOWN VOUCHER — transaction entries for a specific item at a supplier godown
    // Column index: [0] ledger_id, [1] txn_date, [2] txn_type, [3] vch_no,
    //               [4] particulars (branch_name), [5] inward_qty, [6] outward_qty, [7] rate
    // ========================================================================
    @Query(value = """
        SELECT
            msl.ledger_id,
            msl.txn_date,
            msl.txn_type,
            pvh.voucher_number     AS vch_no,
            b.branch_name          AS particulars,
            msl.qty_out            AS inward_qty,
            msl.qty_in             AS outward_qty,
            msl.rate
        FROM material_stock_ledger msl
        JOIN purchase_voucher_header pvh
          ON msl.ref_id = pvh.pv_id
         AND msl.txn_type IN (
             'PV_JW_OUT','PV_JW_IN','PV_JW_OUT_REVERSAL','PV_JW_IN_REVERSAL',
             'PV_DELIVERY_NOTE','PV_DELIVERY_NOTE_REVERSAL',
             'PV_RECEIPT_NOTE','PV_RECEIPT_NOTE_REVERSAL',
             'PV_MATERIAL_IN','PV_MATERIAL_IN_REVERSAL',
             'PV_MATERIAL_OUT','PV_MATERIAL_OUT_REVERSAL',
             'PV_REJECTION_IN','PV_REJECTION_IN_REVERSAL',
             'PV_REJECTION_OUT','PV_REJECTION_OUT_REVERSAL')
        LEFT JOIN supplier_godown_map sgm_dst ON pvh.destination_godown_id = sgm_dst.id
        LEFT JOIN supplier_godown_map sgm_src ON pvh.source_godown_id      = sgm_src.id
        LEFT JOIN branch_master b ON msl.branch_id = b.branch_id
        WHERE (:godownId IS NULL OR pvh.destination_godown_id = :godownId OR pvh.source_godown_id = :godownId)
          AND (:suppId IS NULL OR sgm_dst.supp_id = :suppId OR sgm_src.supp_id = :suppId)
          AND (pvh.destination_godown_id IS NOT NULL OR pvh.source_godown_id IS NOT NULL)
          AND msl.item_id = :itemId
          AND (:fromDate IS NULL OR msl.txn_date >= :fromDate)
          AND (:toDate   IS NULL OR msl.txn_date <= :toDate)
        ORDER BY msl.txn_date ASC, msl.ledger_id ASC
        """, nativeQuery = true)
    List<Object[]> findSupplierGodownVoucherEntries(
            @Param("suppId") String suppId,
            @Param("godownId") Long godownId,
            @Param("itemId") String itemId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
