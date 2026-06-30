package com.fitcore.api.domain.nutrition.repository;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import com.fitcore.api.domain.nutrition.response.DietDailyAggregationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLogEntity, String> {

    List<DietLogEntity> findByUserIdAndLogDate(String userId, LocalDate logDate);

    @Query("""
            SELECT new com.fitcore.api.domain.nutrition.response.DietDailyAggregationResponse(
                e.logDate, SUM(e.kcal), SUM(e.carbsG), SUM(e.proteinG), SUM(e.fatG), COUNT(e.id))
            FROM DietLogEntity e
            WHERE e.userId = :userId AND e.logDate BETWEEN :from AND :to
            GROUP BY e.logDate
            ORDER BY e.logDate
            """)
    List<DietDailyAggregationResponse> findDailyAggregation(
            @Param("userId") String userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
