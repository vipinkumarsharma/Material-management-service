package com.countrydelight.mms.service.stock;

import com.countrydelight.mms.entity.inward.GrnDetail;
import com.countrydelight.mms.entity.outward.IssueFifoConsumption;
import com.countrydelight.mms.exception.InsufficientStockException;
import com.countrydelight.mms.repository.inward.GrnDetailRepository;
import com.countrydelight.mms.repository.outward.IssueFifoConsumptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * FIFO (First-In-First-Out) Service for stock consumption.
 *
 * FIFO Logic:
 * 1. When issuing stock, consume from oldest GRN first
 * 2. Track consumption against each GRN batch
 * 3. Rate for issue is derived from consumed GRN rates (weighted average)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FifoService {

    private final GrnDetailRepository grnDetailRepository;
    private final IssueFifoConsumptionRepository fifoConsumptionRepository;

    /**
     * Result of FIFO consumption containing the consumptions and weighted average rate.
     */
    public record FifoConsumptionResult(
            List<IssueFifoConsumption> consumptions,
            BigDecimal weightedAverageRate
    ) {}

    /**
     * Consume stock using FIFO method.
     *
     * @param branchId      Branch ID
     * @param itemId        Item ID
     * @param locationId    Location ID
     * @param issueId       Issue ID for reference
     * @param qtyToConsume  Quantity to consume
     * @return FifoConsumptionResult with consumptions and weighted average rate
     * @throws InsufficientStockException if not enough stock available
     */
    @Transactional
    public FifoConsumptionResult consumeStock(String branchId, String itemId, String locationId,
                                               Long issueId, BigDecimal qtyToConsume) {
        // Get available GRN batches in FIFO order (oldest first)
        List<GrnDetail> availableBatches = grnDetailRepository
                .findAvailableStockForFifo(branchId, itemId, locationId);

        // Calculate total available
        BigDecimal totalAvailable = availableBatches.stream()
                .map(GrnDetail::getQtyRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(qtyToConsume) < 0) {
            throw new InsufficientStockException(itemId, locationId, qtyToConsume, totalAvailable);
        }

        List<IssueFifoConsumption> consumptions = new ArrayList<>();
        BigDecimal remainingToConsume = qtyToConsume;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (GrnDetail batch : availableBatches) {
            if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal available = batch.getQtyRemaining();
            BigDecimal toConsume = available.min(remainingToConsume);

            // Create consumption record
            IssueFifoConsumption consumption = IssueFifoConsumption.builder()
                    .issueId(issueId)
                    .itemId(itemId)
                    .grnId(batch.getGrnId())
                    .qtyConsumed(toConsume)
                    .rate(batch.getRate())
                    .build();
            consumptions.add(consumption);

            // Update GRN remaining quantity
            batch.setQtyRemaining(available.subtract(toConsume));
            grnDetailRepository.save(batch);

            // Track for weighted average calculation
            totalValue = totalValue.add(toConsume.multiply(batch.getRate()));
            remainingToConsume = remainingToConsume.subtract(toConsume);

            log.debug("FIFO: Consumed {} from GRN {} at rate {}, remaining in batch: {}",
                    toConsume, batch.getGrnId(), batch.getRate(), batch.getQtyRemaining());
        }

        // Save all consumption records
        consumptions = fifoConsumptionRepository.saveAll(consumptions);

        // Calculate weighted average rate
        BigDecimal weightedAvgRate = totalValue.divide(qtyToConsume, 4, RoundingMode.HALF_UP);

        log.info("FIFO consumption complete: Item={}, Qty={}, WeightedAvgRate={}, Batches={}",
                itemId, qtyToConsume, weightedAvgRate, consumptions.size());

        return new FifoConsumptionResult(consumptions, weightedAvgRate);
    }

    /**
     * Check if sufficient stock is available for issue.
     */
    public boolean hasAvailableStock(String branchId, String itemId, String locationId, BigDecimal qtyRequired) {
        List<GrnDetail> availableBatches = grnDetailRepository
                .findAvailableStockForFifo(branchId, itemId, locationId);

        BigDecimal totalAvailable = availableBatches.stream()
                .map(GrnDetail::getQtyRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAvailable.compareTo(qtyRequired) >= 0;
    }

    /**
     * Get available stock quantity for an item at a location.
     */
    public BigDecimal getAvailableStock(String branchId, String itemId, String locationId) {
        List<GrnDetail> availableBatches = grnDetailRepository
                .findAvailableStockForFifo(branchId, itemId, locationId);

        return availableBatches.stream()
                .map(GrnDetail::getQtyRemaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
