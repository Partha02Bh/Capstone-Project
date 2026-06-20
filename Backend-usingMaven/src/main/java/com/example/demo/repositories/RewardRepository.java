package com.example.demo.repositories;

import com.example.demo.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    // All reward entries for a user (history)
    List<Reward> findByUserIdOrderByAwardedAtDesc(Long userId);

    // Total points for a user (sum of all pointsEarned)
    @Query("SELECT COALESCE(SUM(r.pointsEarned), 0) FROM Reward r WHERE r.userId = :userId")
    int sumPointsByUserId(@Param("userId") Long userId);
}