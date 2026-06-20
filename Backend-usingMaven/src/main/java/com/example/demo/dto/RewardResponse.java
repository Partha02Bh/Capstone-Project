package com.example.demo.dto;

import com.example.demo.entity.Reward;
import java.util.List;

public class RewardResponse {

    private Long userId;
    private int totalPoints;
    private List<Reward> history;

    public RewardResponse(Long userId, int totalPoints, List<Reward> history) {
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.history = history;
    }

    public Long getUserId() { return userId; }
    public int getTotalPoints() { return totalPoints; }
    public List<Reward> getHistory() { return history; }
}