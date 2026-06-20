package com.example.demo.controller;

import com.example.demo.dto.RewardResponse;
import com.example.demo.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private static final Logger logger = LoggerFactory.getLogger(RewardController.class);

    @Autowired
    private RewardService rewardService;

    /**
     * GET /api/rewards/{userId}
     * Returns total reward points + full transaction history for the user.
     *
     * Response example:
     * {
     *   "userId": 3,
     *   "totalPoints": 12,
     *   "history": [
     *     { "id": 1, "transactionAmount": 350.00, "pointsEarned": 3, "awardedAt": "2026-02-15 10:22:00" },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/{userId}")
    public ResponseEntity<RewardResponse> getRewards(@PathVariable Long userId) {
        logger.info("Fetching rewards for userId={}", userId);
        RewardResponse response = rewardService.getRewards(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/rewards/{userId}/total
     * Quick endpoint — returns only the total points (no history list).
     *
     * Response: { "userId": 3, "totalPoints": 12 }
     */
    @GetMapping("/{userId}/total")
    public ResponseEntity<Map<String, Object>> getTotalPoints(@PathVariable Long userId) {
        logger.info("Fetching total reward points for userId={}", userId);
        int totalPoints = rewardService.getTotalPoints(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "totalPoints", totalPoints));
    }
}