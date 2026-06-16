package com.fitcore.api.domain.nutrition.service;

import com.fitcore.api.domain.nutrition.entity.DietLogEntity;
import com.fitcore.api.domain.nutrition.repository.DietLogRepository;
import com.fitcore.api.domain.nutrition.request.DietLogRequest;
import com.fitcore.api.domain.nutrition.response.DietLogResponse;
import com.fitcore.api.domain.nutrition.response.DietSummaryResponse;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietLogService {

    private static final BigDecimal PROTEIN_KCAL_PER_G = BigDecimal.valueOf(4);
    private static final BigDecimal CARBS_KCAL_PER_G   = BigDecimal.valueOf(4);
    private static final BigDecimal FAT_KCAL_PER_G     = BigDecimal.valueOf(9);
    private static final DateTimeFormatter TIME_FORMAT  = DateTimeFormatter.ofPattern("HH:mm");

    private final DietLogRepository dietLogRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public List<DietLogResponse> saveBatch(List<DietLogRequest> requests) {
        String userId = securityUtils.getCurrentUserId();
        List<DietLogEntity> entities = requests.stream()
                .map(req -> toEntity(req, userId))
                .toList();
        return dietLogRepository.saveAll(entities).stream()
                .map(DietLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DietSummaryResponse getSummary(LocalDate date) {
        String userId = securityUtils.getCurrentUserId();
        List<DietLogEntity> logs = dietLogRepository.findByUserIdAndLogDate(userId, date);

        List<DietLogEntity> sorted = logs.stream()
                .sorted(Comparator.comparing(
                        DietLogEntity::getLoggedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        int totalKcal = sorted.stream().mapToInt(DietLogEntity::getKcal).sum();
        BigDecimal totalProtein = sumMacro(sorted, true, false, false);
        BigDecimal totalCarbs   = sumMacro(sorted, false, true, false);
        BigDecimal totalFat     = sumMacro(sorted, false, false, true);

        return DietSummaryResponse.builder()
                .date(date)
                .totalKcal(totalKcal)
                .totalProteinG(totalProtein.setScale(1, RoundingMode.HALF_UP))
                .totalCarbsG(totalCarbs.setScale(1, RoundingMode.HALF_UP))
                .totalFatG(totalFat.setScale(1, RoundingMode.HALF_UP))
                .items(sorted.stream().map(DietLogResponse::fromEntity).toList())
                .build();
    }

    private BigDecimal sumMacro(List<DietLogEntity> logs, boolean protein, boolean carbs, boolean fat) {
        return logs.stream()
                .map(e -> {
                    if (protein) return e.getProteinG();
                    if (carbs)   return e.getCarbsG();
                    return e.getFatG();
                })
                .map(v -> v != null ? v : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DietLogEntity toEntity(DietLogRequest req, String userId) {
        int kcal = resolveKcal(req);
        LocalDateTime loggedAt = parseLoggedAt(req.getLogDate(), req.getLoggedAt());

        return DietLogEntity.builder()
                .userId(userId)
                .logDate(req.getLogDate())
                .mealType(req.getMealType())
                .loggedAt(loggedAt)
                .foodName(req.getFoodName())
                .amountG(req.getAmountG())
                .amountRaw(req.getAmountRaw())
                .kcal(kcal)
                .proteinG(req.getProteinG())
                .carbsG(req.getCarbsG())
                .fatG(req.getFatG())
                .source(req.getSource())
                .build();
    }

    private int resolveKcal(DietLogRequest req) {
        boolean hasMacros = req.getProteinG() != null || req.getCarbsG() != null || req.getFatG() != null;

        if ("ai".equals(req.getSource())) {
            // ai: 매크로로 4·4·9 강제 (요청 kcal 무시)
            if (!hasMacros) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return calculateMacroKcal(req);
        }

        // db / manual: 요청 kcal 우선, 없으면 매크로
        if (req.getKcal() != null) {
            return req.getKcal();
        }
        if (!hasMacros) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return calculateMacroKcal(req);
    }

    private int calculateMacroKcal(DietLogRequest req) {
        BigDecimal protein = orZero(req.getProteinG());
        BigDecimal carbs   = orZero(req.getCarbsG());
        BigDecimal fat     = orZero(req.getFatG());

        BigDecimal total = protein.multiply(PROTEIN_KCAL_PER_G)
                .add(carbs.multiply(CARBS_KCAL_PER_G))
                .add(fat.multiply(FAT_KCAL_PER_G));
        return total.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private BigDecimal orZero(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private LocalDateTime parseLoggedAt(LocalDate date, String loggedAtStr) {
        if (loggedAtStr == null || loggedAtStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.of(date, LocalTime.parse(loggedAtStr, TIME_FORMAT));
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
