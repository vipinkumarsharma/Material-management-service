package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.ApprovalRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, String> {

    @Query("SELECT a FROM ApprovalRule a WHERE a.txnType = :txnType")
    Page<ApprovalRule> findByTxnType(@Param("txnType") String txnType, Pageable pageable);

    @Query("SELECT a FROM ApprovalRule a WHERE a.txnType = :txnType AND a.conditionType = :conditionType")
    Page<ApprovalRule> findByTxnTypeAndConditionType(@Param("txnType") String txnType,
                                                     @Param("conditionType") String conditionType,
                                                     Pageable pageable);
}
