package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.VoucherSeriesRestartSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherSeriesRestartScheduleRepository extends JpaRepository<VoucherSeriesRestartSchedule, Long> {

    @Query("""
            SELECT r FROM VoucherSeriesRestartSchedule r
            WHERE r.seriesId = :id
            ORDER BY r.applicableFromDate ASC
            LIMIT 200
            """)
    List<VoucherSeriesRestartSchedule> findBySeriesAsc(@Param("id") String seriesId);

    @Query("""
            SELECT r FROM VoucherSeriesRestartSchedule r
            WHERE r.seriesId = :seriesId
            AND r.applicableFromDate <= :asOfDate
            ORDER BY r.applicableFromDate DESC
            LIMIT 10
            """)
    List<VoucherSeriesRestartSchedule> findApplicableSchedules(
            @Param("seriesId") String seriesId,
            @Param("asOfDate") LocalDate asOfDate);

    @Query("""
            SELECT r FROM VoucherSeriesRestartSchedule r
            WHERE r.seriesId = :seriesId
            AND r.applicableFromDate <= :asOfDate
            ORDER BY r.applicableFromDate DESC
            LIMIT 1
            """)
    Optional<VoucherSeriesRestartSchedule> findLatestApplicableSchedule(
            @Param("seriesId") String seriesId,
            @Param("asOfDate") LocalDate asOfDate);
}
