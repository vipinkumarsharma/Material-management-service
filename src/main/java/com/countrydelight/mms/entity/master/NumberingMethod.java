package com.countrydelight.mms.entity.master;

/**
 * Controls how voucher numbers are assigned to documents of a given Voucher Type.
 * Mirrors the five methods available in TallyPrime.
 */
public enum NumberingMethod {

    /**
     * System auto-generates the next sequential number from the configured Voucher Series.
     * Numbers are renumbered or retained (based on numberingOnDeletion) when vouchers are added/deleted.
     */
    AUTOMATIC,

    /**
     * Same as AUTOMATIC, but the user can manually override the pre-filled number at entry time.
     * Subsequent numbers continue from the overridden value; skipped numbers can be reused later.
     * Duplicate prevention is controlled by preventDuplicates.
     */
    AUTOMATIC_MANUAL_OVERRIDE,

    /**
     * User must enter the voucher number manually for every transaction.
     * No sequence is enforced; preventDuplicates controls whether repeats are blocked.
     */
    MANUAL,

    /**
     * Extension of AUTOMATIC for concurrent multi-user environments.
     * Numbers are allocated sequentially even when multiple users save vouchers simultaneously.
     * In MMS this is backed by a PESSIMISTIC_WRITE lock on the series row.
     */
    MULTI_USER_AUTO,

    /**
     * No numbering — voucher_number is always null for this type.
     */
    NONE
}
