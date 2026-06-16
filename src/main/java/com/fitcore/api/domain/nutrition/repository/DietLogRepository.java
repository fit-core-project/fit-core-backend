package com.fitcore.api.domain.nutrition.repository;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLogEntity, String> {

    List<DietLogEntity> findByUserIdAndLogDate(String userId, LocalDate logDate);
}
