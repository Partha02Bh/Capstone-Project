package com.example.demo.service;

import com.example.demo.dto.RewardResponse;
import com.example.demo.entity.Reward;
import com.example.demo.repositories.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RewardService {

    private static final Logger logger = LoggerFactory.getLogger(RewardService.class);

    // Reward is granted only when transfer amount STRICTLY exceeds ₹100
    private static final BigDecimal REWARD_THRESHOLD = new BigDecimal("100");

    // 1 point per ₹100 transferred
    private static final BigDecimal POINTS_PER_HUNDRED = new BigDecimal("100");

    @Autowired
    private RewardRepository rewardRepo;

    /**
     * Eligibility Rule:
     *   1. Amount must be STRICTLY GREATER than ₹100
     *   (Status=SUCCESS, different users, not self-transfer are
     *    already guaranteed by TransactionController before this is called)
     */
    private boolean isEligible(BigDecimal amount) {
        return amount.compareTo(REWARD_THRESHOLD) > 0;
    }

    /**
     * Reward Calculation:
     *   1 point per ₹100 transferred, rounded DOWN
     *   e.g. ₹350 → 3 points, ₹250.50 → 2 points
     */
    public int calculatePoints(BigDecimal amount) {
        return amount.divide(POINTS_PER_HUNDRED, 0, RoundingMode.FLOOR).intValue();
    }

    /**
     * Called after every successful transfer.
     * Checks eligibility, calculates points, and persists the reward entry.
     *
     * @param userId            sender's user ID (earns the reward)
     * @param accountId         sender's account ID
     * @param transactionId     ID of the TRANSFER_OUT TransactionLog entry
     * @param amount            transfer amount (positive)
     */
    public void processReward(Long userId, Long accountId, Long transactionId, BigDecimal amount) {
        if (!isEligible(amount)) {
            logger.info("Transfer not eligible for reward: userId={}, amount={}", userId, amount);
            return;
        }

        int points = calculatePoints(amount);

        Reward reward = new Reward();
        reward.setUserId(userId);
        reward.setAccountId(accountId);
        reward.setTransactionId(transactionId);
        reward.setTransactionAmount(amount);
        reward.setPointsEarned(points);
        reward.setAwardedAt(LocalDateTime.now());

        rewardRepo.save(reward);
        logger.info("Reward granted: userId={}, transactionId={}, amount={}, points={}",
                userId, transactionId, amount, points);
    }

    /**
     * Returns total points + full reward history for a user.
     */
    public RewardResponse getRewards(Long userId) {
        List<Reward> history = rewardRepo.findByUserIdOrderByAwardedAtDesc(userId);
        int totalPoints = rewardRepo.sumPointsByUserId(userId);
        return new RewardResponse(userId, totalPoints, history);
    }

    /**
     * Returns total accumulated points for a user (quick lookup).
     */
    public int getTotalPoints(Long userId) {
        return rewardRepo.sumPointsByUserId(userId);
    }
}